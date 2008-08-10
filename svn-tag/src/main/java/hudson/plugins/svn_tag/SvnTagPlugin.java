package hudson.plugins.svn_tag;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Result;
import hudson.scm.SubversionSCM;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCopyClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


/**
 * Consolidates the work common in Publisher and MavenReporter.
 * @author Kenji Nakamura
 */
@SuppressWarnings({"UtilityClass", "ImplicitCallToSuper", "MethodReturnOfConcreteClass", "MethodParameterOfConcreteClass", "InstanceofInterfaces", "unchecked"})
public class SvnTagPlugin {
    
    /**
     * Description appeared in configuration screen.
     */
    public static final String DESCRIPTION =
            "Perform Subversion tagging on successful build";

    /**
     * The prefix to identify jelly variables for this plugin.
     */
    public static final String CONFIG_PREFIX = "svntag.";

    /**
     * Creates a new SvnTagPlugin object.
     */
    private SvnTagPlugin() {
    }

    /**
     * Returns the root project value.
     *
     * @param project the given project value.
     * @return the root project value.
     */
    @SuppressWarnings({"MethodParameterOfConcreteClass"})
    private static AbstractProject getRootProject(AbstractProject project) {
        //noinspection InstanceofInterfaces
        if (project.getParent() instanceof Hudson) {
            return project;
        } else {
            //noinspection CastToConcreteClass
            return getRootProject((AbstractProject) project.getParent());
        }
    }

    /**
     * True if the operation was successful.
     *
     * @param abstractBuild build
     * @param launcher      launcher
     * @param buildListener build listener
     * @param tagBaseURLStr tag base URL string
     * @param tagComment    tag comment
     * @return true if the operation was successful
     */
    @SuppressWarnings({"FeatureEnvy", "UnusedDeclaration", "TypeMayBeWeakened", "LocalVariableOfConcreteClass"})
    public static boolean perform(AbstractBuild abstractBuild,
                                  Launcher launcher,
                                  BuildListener buildListener,
                                  String tagBaseURLStr, String tagComment) {
        PrintStream logger = buildListener.getLogger();

        if (!Result.SUCCESS.equals(abstractBuild.getResult())) {
            logger.println("No Subversion tagging for unsuccessful build. ");

            return true;
        }

        AbstractProject<?, ?> rootProject =
                getRootProject(abstractBuild.getProject());

        Map<String, String> env;

        if (!(rootProject.getScm() instanceof SubversionSCM)) {
            logger.println("SvnTag plugin doesn't support tagging for SCM " +
                    rootProject.getScm().toString() + ".");

            return true;
        }

        SubversionSCM scm = SubversionSCM.class.cast(rootProject.getScm());
        env = abstractBuild.getEnvVars();

        // Let SubversionSCM fill revision number.
        // It is guaranteed for getBuilds() return the latest build (i.e.
        // current build) at first
        // The passed in abstractBuild may be the sub maven module and not
        // have revision.txt holding Svn revision information, so need to use
        // the build associated with the root level project.
        scm.buildEnvVars(rootProject.getBuilds().get(0), env);

        SubversionSCM.ModuleLocation[] moduleLocations = scm.getLocations();

        tagBaseURLStr = evalGroovyExpression(env, tagBaseURLStr);

        // environment variable "SVN_REVISION" doesn't contain revision number when multiple modules are
        // specified. Instead, parse revision.txt and obtain the corresponding revision numbers.
        Map<String,Long> revisions = null;
        try {
            revisions = parseRevisionFile(abstractBuild);
        } catch (IOException e) {
            logger.println("Failed to parse revision.txt. " + e.getLocalizedMessage());
            return false;
        }

        ISVNAuthenticationProvider sap =  scm.getDescriptor().createAuthenticationProvider();
        if(sap == null) {
            logger.println("Subversion authentication info is not set.");
            return false;
        }
        
        ISVNAuthenticationManager sam = SVNWCUtil.createDefaultAuthenticationManager();
        sam.setAuthenticationProvider(sap);

        SVNCommitClient commitClient = new SVNCommitClient(sam, null);


        for (SubversionSCM.ModuleLocation ml : moduleLocations) {
            logger.println("moduleLocation: Remote ->" + ml.remote);

            URI repoURI = null;
            try {
                repoURI = new URI(ml.remote);
            } catch (URISyntaxException e) {
                logger.println("Failed to parse SVN repo URL. " + e.getLocalizedMessage());
                return false;
            }

            SVNURL parsedTagBaseURL = null;
            try {
                parsedTagBaseURL = SVNURL.parseURIEncoded(repoURI.resolve(tagBaseURLStr).toString());
            } catch (SVNException e) {
                logger.println("Failed to parse tag base URL '" + tagBaseURLStr + "'. " + e.getLocalizedMessage());
            }

            try {
                SVNCommitInfo deleteInfo =
                        commitClient.doDelete(new SVNURL[]{ parsedTagBaseURL},
                                "Delete old tag by SvnTag Hudson plugin.");
                SVNErrorMessage deleteErrMsg = deleteInfo.getErrorMessage();

                if (null != deleteErrMsg) {
                    logger.println(deleteErrMsg.getMessage());
                } else {
                    logger.println("Delete old tag " + tagBaseURLStr + ".");
                }

                SVNCommitInfo mkdirInfo =
                        commitClient.doMkDir(new SVNURL[]{parsedTagBaseURL},
                                "Created by SvnTag Hudson plugin.");
                SVNErrorMessage mkdirErrMsg = mkdirInfo.getErrorMessage();

                if (null != mkdirErrMsg) {
                    logger.println(mkdirErrMsg.getMessage());

                    return false;
                }
            } catch (SVNException e) {
                logger.println("There was no old tag at " + tagBaseURLStr + ".");
            }

            SVNCopyClient copyClient = new SVNCopyClient(sam, null);

            try {
                String evalComment = evalGroovyExpression((Map<String, String>) abstractBuild.getEnvVars(), tagComment);

                SVNCommitInfo commitInfo =
                        copyClient.doCopy(SVNURL.parseURIEncoded(ml.remote),
                                SVNRevision.create(Long.valueOf(revisions.get(ml.remote))),
                                parsedTagBaseURL, false,
                                false, evalComment);
                SVNErrorMessage errorMsg = commitInfo.getErrorMessage();

                if (null != errorMsg) {
                    logger.println(errorMsg.getFullMessage());

                    return false;
                } else {
                    logger.println("Tagged as Revision " +
                            commitInfo.getNewRevision());
                }
            } catch (SVNException e) {
                logger.println("Subversion copy failed. " +
                        e.getLocalizedMessage());

                return false;
            }
        }

        return true;
    }

    @SuppressWarnings({"StaticMethodOnlyUsedInOneClass", "TypeMayBeWeakened"})
    static String evalGroovyExpression(Map<String, String> env, String tagComment) {
        Binding binding = new Binding();
        binding.setVariable("env", env);
        binding.setVariable("sys", System.getProperties());
        CompilerConfiguration config = new CompilerConfiguration();
        //config.setDebug(true);
        GroovyShell shell = new GroovyShell(binding, config);
        Object result = shell.evaluate("return \"" + tagComment + "\"");
        if (result == null) {
            return "";
        } else {
            return result.toString().trim();
        }
    }


    /**
     * Reads the revision file of the specified build.
     *
     * @return
     *      map from {@link SvnInfo#url Subversion URL} to its revision.
     */
    /*package*/ @SuppressWarnings({"NestedAssignment"})
    static Map<String,Long> parseRevisionFile(AbstractBuild build) throws IOException {
        Map<String,Long> revisions = new HashMap<String,Long>(); // module -> revision
        // read the revision file of the last build
        File file = SubversionSCM.getRevisionFile(build);
        if(!file.exists()) // nothing to compare against
        {
            return revisions;
        }

        BufferedReader br = new BufferedReader(new FileReader(file));
        try {
            String line;
            while((line=br.readLine())!=null) {
                int index = line.lastIndexOf('/');
                if(index<0) {
                    continue;   // invalid line?
                }
                try {
                    revisions.put(line.substring(0,index), Long.parseLong(line.substring(index+1)));
                } catch (NumberFormatException e) {
                    // perhaps a corrupted line. ignore
                }
            }
        } finally {
            br.close();
        }

        return revisions;
    }
}
