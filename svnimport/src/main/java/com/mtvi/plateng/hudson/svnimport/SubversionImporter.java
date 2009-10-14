package com.mtvi.plateng.hudson.svnimport;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;

public class SubversionImporter extends Notifier {

    private static final Logger LOGGER = Logger.getLogger(SubversionImporter.class.getName());

    public final String artifactExpression;

    private final Pattern artifactPattern;

    public final String baseUrl;

    private final SVNURL baseSvnUrl;

    public final String importExpression;

    private final SVNClientManager svnClientManager;

    @DataBoundConstructor
    public SubversionImporter(String artficatExpression, String baseUrl, String importExpression)
            throws SVNException {
        this.artifactExpression = artficatExpression;
        this.artifactPattern = Pattern.compile(artifactExpression);
        this.baseUrl = baseUrl;
        baseSvnUrl = SVNURL.parseURIEncoded(baseUrl);
        this.importExpression = importExpression;
        this.svnClientManager = SVNClientManager.newInstance();
    }

    public List<ImportItem> getItemsToImport(FilePath workspace) throws SVNException, IOException,
            InterruptedException {
        String workspaceURI = workspace.toURI().toString();
        FilePath[] files = workspace.list("**/*");

        List<ImportItem> items = new ArrayList<ImportItem>();
        for (FilePath file : files) {
            String filePath = file.toURI().toString().substring(workspaceURI.length() - 1);

            String output = importExpression;

            Matcher matcher = artifactPattern.matcher(filePath);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    output = output.replace("$" + i, matcher.group(i));
                }
                items.add(new ImportItem(file, baseSvnUrl.appendPath(output, false)));
            }
        }
        return items;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        try {
            if (build.getResult() == Result.SUCCESS) {
                FilePath workspace = build.getWorkspace();

                List<ImportItem> items = getItemsToImport(workspace);
                for (final ImportItem importItem : items) {
                    importItem.path.act(new FileCallable<Boolean>() {

                        public Boolean invoke(File f, VirtualChannel channel) throws IOException {
                            try {
                                svnClientManager.getCommitClient().doImport(f,
                                        importItem.svnDestination, "", null, false, false, SVNDepth.EMPTY);
                                return true;
                            } catch (SVNException e) {
                                throw new IOException("Unable to import " + f.getAbsolutePath()
                                        + " " + e.getMessage());
                            }
                        }
                    });
                }

            }
        } catch (SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(SubversionImporter.class);
        }

        @Override
        public String getDisplayName() {
            return "Subversion Importer";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }

    public class ImportItem {
        public final FilePath path;
        public final SVNURL svnDestination;

        public ImportItem(FilePath path, SVNURL svnDestination) {
            this.path = path;
            this.svnDestination = svnDestination;
        }
    }
}
