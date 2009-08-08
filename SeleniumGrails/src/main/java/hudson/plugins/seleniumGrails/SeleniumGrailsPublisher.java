package hudson.plugins.seleniumGrails;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;
import hudson.AbortException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import javax.servlet.ServletException;


import org.apache.tools.ant.DirectoryScanner;
import org.kohsuke.stapler.DataBoundConstructor;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;
/**
 * Clover {@link Publisher}.
 * 
 * @author Pascal Martin
 */
public class SeleniumGrailsPublisher extends Publisher implements Serializable {

	public String baseDir;

	@DataBoundConstructor
	public SeleniumGrailsPublisher(String baseDir){
		this.baseDir = baseDir;
	}


	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
		final long buildTime = build.getTimestamp().getTimeInMillis();
		FileSet resultFileSet = Util.createFileSet(new File(build.getProject().getWorkspace().toString()), "**/seleniumTestResult.xml");
		DirectoryScanner ds = resultFileSet.getDirectoryScanner();
		String[] files = ds.getIncludedFiles();
    if(files.length==0) {
      throw new AbortException("No test report files were found. Configuration error?");
	  }
		SeleniumGrailsTestResultAction action = new SeleniumGrailsTestResultAction(build, build.getProject().getWorkspace().toString()+"/"+files[0]);
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

	public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
  }
	
	public String getDisplayName(){
		return "DFSSDAFDSAFSAD";
	}

	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
		
  public static class DescriptorImpl extends Descriptor<Publisher> {
		public DescriptorImpl(){
			super(SeleniumGrailsPublisher.class);
		}
		public String getDisplayName() {
    		return "Publish Selenium-Grails Report";
    	}
	}
}
