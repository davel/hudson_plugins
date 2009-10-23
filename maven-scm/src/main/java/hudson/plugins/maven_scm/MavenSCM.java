package hudson.plugins.maven_scm;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.scm.ChangeLogParser;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class MavenSCM extends SCM {
    /**
     * Maven SCM URL.
     */
    public final String scmUrl;

    public MavenSCM(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    /**
     * Gets the provider ID of SCM URL that identifies the provider,
     * such as "bazaar"
     *
     * @return
     *      null if the prefix matching fails, which is most likely an operator error.
     */
    public String getProvider() {
        Matcher m = PREFIX_PATTERN.matcher(scmUrl);
        if(m.find(0))
            return m.group(1);
        else
            return null;
    }

    private static final Pattern PREFIX_PATTERN = Pattern.compile("scm:([a-z]+)[:|]");

    @Override
    public boolean supportsPolling() {
        return false;
    }

    @Override
    public boolean pollChanges(AbstractProject project, Launcher launcher, FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean checkout(AbstractBuild build, Launcher launcher, FilePath workspace, final BuildListener listener, File changelogFile) throws IOException, InterruptedException {
        final Date to = new Date(build.getTimestamp().getTimeInMillis());
        final Date from;
        if(build.getPreviousBuild()!=null)
            from = new Date(build.getPreviousBuild().getTimestamp().getTimeInMillis());
        else
            from = null;

        return workspace.act(new FileCallable<Boolean>() {
            public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
                try {
                    ScmManager scmManager = PluginImpl.MANAGER;
                    ScmRepository repo = getScmRepository(scmManager);
                    CheckOutScmResult result = scmManager.checkOut(repo, new ScmFileSet(ws) );
                    if(!result.isSuccess()) {
                        listener.getLogger().println("Failed to checkout");
                        dumpFailure(result,listener);
                        return false;
                    }

                    // remember what we've checked out
                    FileUtils.writeStringToFile(new File(ws,".scmurl"),scmUrl,"UTF-8");

                    ChangeLogScmResult changelogResult = scmManager.changeLog(repo, new ScmFileSet(ws), from, to, -1, null);
                    if(!changelogResult.isSuccess()) {
                        listener.getLogger().println("Failed to compute changelog");
                        dumpFailure(changelogResult,listener);
                        return false;
                    }

                    return true;
                } catch (ScmException e) {
                    e.printStackTrace(listener.error("Failed to check out"));
                    return false;
                }
            }
        });
    }

    public ChangeLogParser createChangeLogParser() {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * Use the best descriptor depending on our URL.
     */
    @Override
    public SCMDescriptor<?> getDescriptor() {
        String provider = getProvider();
        if(provider==null)
            return GenericMavenSCMDescriptor.INSTANCE;

        for (SCMDescriptor<?> d : SCM.all()) {
            if(d instanceof ProviderSpecificDescriptor
            && ((ProviderSpecificDescriptor)d).provider.equals(provider))
                return d;
        }

        return GenericMavenSCMDescriptor.INSTANCE;
    }

    private void dumpFailure(ScmResult result, TaskListener listener) {
        PrintStream logger = listener.getLogger();
        if(result.getProviderMessage()!=null) {
            logger.println("Provider message:");
            logger.println(result.getProviderMessage());
        }
        if(result.getCommandLine()!=null) {
            logger.println("Command line: "+result.getCommandLine());
        }
        if(result.getCommandOutput()!=null) {
            logger.println("Command output:");
            logger.println(result.getCommandOutput());
        }
    }

    private ScmRepository getScmRepository(ScmManager scmManager) throws IOException {
        try {
            return scmManager.makeScmRepository(scmUrl);
        } catch (NoSuchScmProviderException ex) {
            throw new IOException("Could not find the SCM provider for "+scmUrl,ex);
        } catch (ScmRepositoryException ex) {
            throw new IOException("Error while connecting to "+scmUrl,ex);
        }
    }
}
