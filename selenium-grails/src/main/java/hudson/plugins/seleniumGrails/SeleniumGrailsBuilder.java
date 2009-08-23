package hudson.plugins.seleniumGrails;

import hudson.tasks.Builder;
import hudson.model.Result;
import hudson.model.Descriptor;
import hudson.model.BuildListener;
import hudson.model.Build;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.util.Map;
import java.io.IOException;
public class SeleniumGrailsBuilder extends Builder {
	
	private String browser;
	private String grailsHome;
	private String baseDir;
	private String projectName;

	public String getBrowser(){return browser;}
	public void setBrowser(String browser){this.browser = browser;}
	public String getGrailsHome(){return grailsHome;}
	public void setGrailsHome(String grailsHome){this.grailsHome = grailsHome;}
	public String getBaseDir(){return baseDir;}
	public void setBaseDir(String baseDir){this.baseDir = baseDir;}
	public String getProjectName(){return projectName;}
	public void setProjectName(String projectName){this.projectName = projectName;}

	@DataBoundConstructor
	public SeleniumGrailsBuilder(String browser, String baseDir, String grailsHome, String projectName){
		this.browser = browser;
		this.baseDir = baseDir;
		this.grailsHome = grailsHome;
		this.projectName = projectName;
	}
	
	public boolean perform(Build build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
		if (browser == null || browser.length() == 0) {
    	listener.error("Build config : browser field is mandatory");
      build.setResult(Result.FAILURE);
      return false;
		}
		if(baseDir == null || baseDir.length() == 0){
			listener.getLogger().println("Base Dir not set, defaulting to \".\"");
			baseDir = ".";
		}
		if(grailsHome == null || grailsHome.length() == 0){
			listener.error("Build config : GRAILS_HOME field is mandatory");
      build.setResult(Result.FAILURE);
      return false;
		}
		if(projectName == null || projectName.length() == 0){
      listener.error("Build config : Project name field is mandatory");
      build.setResult(Result.FAILURE);
      return false;
    }


		Map<String, String> env = build.getEnvVars();
		env.put("GRAILS_HOME", grailsHome);
		
		String startGrailsCommand = String.format("%1$s/bin/grails -Dserver.port=64381 run-app",grailsHome);
		Proc grailsServer = launcher.launch(startGrailsCommand, env, listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));
		Thread.sleep(30000);//give hte grails server some time to start up
		
		FilePath resultFile = new FilePath(new FilePath(build.getProject().getModuleRoot(), baseDir), "seleniumTestResult.xml");
		if(resultFile.exists()) resultFile.delete();

		String cmd = browser + " http://localhost:64381/"+projectName+"/selenium/core/TestRunner.html?test=..%2F..%2Fselenium/suite&auto=on&close=on&resultsUrl=..%2FpostResults%3Ffile%3DseleniumTestResult.xml";
		Proc browser = launcher.launch(cmd, env, listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));

		while(!resultFile.exists()){ Thread.sleep(1000); } //wait for the result file to be written

		browser.kill();
		grailsServer.kill();
		return true;
	}
	
	public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
  }
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
	public static final class DescriptorImpl extends Descriptor<Builder> {
		DescriptorImpl() {
    	super(SeleniumGrailsBuilder.class);
    }
		
		public String getDisplayName() {
            return "Selenium grails";
    }
	}
}
