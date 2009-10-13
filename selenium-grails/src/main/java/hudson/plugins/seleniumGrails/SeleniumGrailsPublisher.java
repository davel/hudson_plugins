package hudson.plugins.seleniumGrails;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;

import hudson.AbortException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;
/**
 * Clover {@link Publisher}.
 * 
 * @author Pascal Martin
 */
public class SeleniumGrailsPublisher extends Recorder implements Serializable {

	public String baseDir;

	@DataBoundConstructor
	public SeleniumGrailsPublisher(String baseDir){
		this.baseDir = baseDir;
	}

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.BUILD;
	}


	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
		final long buildTime = build.getTimestamp().getTimeInMillis();
		FileSet resultFileSet = Util.createFileSet(new File(build.getWorkspace().getRemote()), "**/seleniumTestResult.xml");
		DirectoryScanner ds = resultFileSet.getDirectoryScanner();
		String[] files = ds.getIncludedFiles();
    if(files.length==0) {
      throw new AbortException("No test report files were found. Configuration error?");
	  }
		SeleniumGrailsTestResultAction action = new SeleniumGrailsTestResultAction(build, build.getWorkspace().getRemote()+"/"+files[0]);
		build.getActions().add(action);
		if(action.getFailCount()>0)
            build.setResult(Result.UNSTABLE);
/*    String[] resultFiles = ds.getIncludedFiles();
		File rootTarget = getSeleniumReportDir(build.getParent());
		
		for(String resultFile : resultFiles){
			System.out.println("resultFile: "+resultFile);
	    new FilePath(rootTarget).deleteContents();
			//TODO: all thede new FilePtahs is just silly
			new FilePath(new File(build.getProject().getWorkspace().toString()+"/"+resultFile)).copyTo(new FilePath(new FilePath(rootTarget), "index.html"));
		}*/
		return true;
	}

	public static File getSeleniumReportDir(AbstractItem project) {
  	return new File(project.getRootDir(), "seleniumGrails");
  }

	public String getDisplayName(){
		return "DFSSDAFDSAFSAD";
	}

  @Extension
  public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		public DescriptorImpl(){
			super(SeleniumGrailsPublisher.class);
		}
		public String getDisplayName() {
    		return "Publish Selenium-Grails Report";
    	}

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }
	}
}
