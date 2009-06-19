package hudson.plugins.javanet_uploader;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractProject;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.jnt.JNFile;
import org.kohsuke.jnt.JNFileFolder;
import org.kohsuke.jnt.JNProject;
import org.kohsuke.jnt.JavaNet;
import org.kohsuke.jnt.ProcessingException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * {@link Publisher} that uploads files to java.net documents and files section.
 *
 * @author Kohsuke Kawaguchi
 */
public class JNUploaderPublisher extends Recorder {

    /**
     * Name of the java.net project to post a file to.
     */
    public final String project;

    private final List<Entry> entries;

    @DataBoundConstructor
    public JNUploaderPublisher(String project, List<Entry> entries) {
        this.project = project;
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException {
        if(build.getResult()== Result.FAILURE) {
            // build failed. don't post
            return true;
        }

        try {
            listener.getLogger().println("Connecting to java.net");
            JavaNet jn = JavaNet.connect();
            JNProject project = jn.getProject(this.project);
            if(!project.exists()) {
                listener.error("No such project exists: "+project);
                build.setResult(Result.FAILURE);
                return true;
            }

            EnvVars envVars = build.getEnvironment(listener);

            for (Entry e : entries) {
                if(e.sourceFile.trim().length()==0) {
                    listener.getLogger().println("Configuration error: no file is specified for upload");
                    build.setResult(Result.FAILURE);
                    return true;
                }
                if(e.filePath.contains("/servlets/ProjectDocumentList?")) {
                    // some people tried to specify URL here
                    listener.getLogger().println("Configuration error: specify a folder path like '/foo/bar/zot' in the destination, not URL");
                    build.setResult(Result.FAILURE);
                }

                listener.getLogger().println("Uploading "+e.sourceFile+" to java.net");

                String expanded = Util.replaceMacro(e.sourceFile, envVars);
                FilePath[] src = build.getProject().getWorkspace().list(expanded);
                if(src.length==0)
                    throw new ProcessingException("No such file exists: "+ expanded);

                if(src.length==1) {
                    String folderPath = Util.replaceMacro(e.filePath,envVars);
                    JNFileFolder folder = project.getFolder(folderPath);
                    if(folder!=null) {
                        // this looks like a valid folder name, so just upload into it
                        upload(folder, src[0].getName(), src[0], e, envVars, listener);
                    } else {
                        // assume that this is a full path name
                        int idx = folderPath.lastIndexOf('/');

                        if(idx<0)
                            throw new ProcessingException(folderPath+" doesn't have a file name");

                        String fileName = folderPath.substring(idx+1);
                        folderPath = folderPath.substring(0,idx);

                        folder = getFolder(project, folderPath);

                        upload(folder, fileName, src[0], e, envVars, listener);
                    }
                } else {
                    String folderPath = Util.replaceMacro(e.filePath,envVars);
                    JNFileFolder folder = getFolder(project,folderPath);

                    for( FilePath s : src )
                        upload(folder, s.getName(), s, e, envVars, listener);
                }
            }
        } catch (ProcessingException e) {
            e.printStackTrace(listener.error("Failed to access java.net"));
            build.setResult(Result.FAILURE);
        } catch (IOException e) {
            e.printStackTrace(listener.error("Failed to upload files"));
            build.setResult(Result.FAILURE);
        }

        return true;
    }

    private void upload(JNFileFolder folder, String fileName, FilePath src, Entry e, Map<String, String> envVars, BuildListener listener) throws ProcessingException, IOException, InterruptedException {
        JNFile file = folder.getFiles().get(fileName);
        if( file!=null ) {
            file.delete();
        }

        // java.net upload occasionally fails for no good reason, so retry for a few times under a certain situation
        for( int retryCount=0; ; retryCount++) {
            InputStream in = src.read();
            try {
                folder.uploadFile(fileName,
                                Util.replaceMacro(e.description,envVars), e.status, in, "application/octet-stream");
                return;
            } catch (ProcessingException x) {
                if(x.getMessage().contains("timed out") && retryCount<3) {
                    // retry for a few times in case of time out
                    listener.error("Upload timed out. Retrying after 10 seconds");
                    Thread.sleep(10*1000);
                    continue;
                }
                throw x;
            } finally {
                in.close();
            }
        }
    }

    /**
     * Gets the {@link JNFileFolder} and do the error checking.
     */
    private JNFileFolder getFolder(JNProject project, String folderPath) throws ProcessingException {
        JNFileFolder folder = project.getFolder(folderPath);
        if(folder==null)
            throw new ProcessingException("No such folder "+folderPath+" on project "+this.project);
        return folder;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public String getDisplayName() {
            return "Publish artifacts to java.net";
        }

        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
