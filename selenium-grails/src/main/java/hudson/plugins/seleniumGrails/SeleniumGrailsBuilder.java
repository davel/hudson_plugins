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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import java.util.Map;
import java.io.IOException;
import java.io.File;

public class SeleniumGrailsBuilder extends Builder {
	
	private String browser;
	private String grailsHome;
	private String baseDir;
	private String projectName;
	private String deployString;
	private String port;
	private String resultPath;

	public String getBrowser(){return browser;}
	public void setBrowser(String browser){this.browser = browser;}
	public String getGrailsHome(){return grailsHome;}
	public void setGrailsHome(String grailsHome){this.grailsHome = grailsHome;}
	public String getBaseDir(){return baseDir;}
	public void setBaseDir(String baseDir){this.baseDir = baseDir;}
	public String getProjectName(){return projectName;}
	public void setProjectName(String projectName){this.projectName = projectName;}
	public String getDeployString(){return deployString;}
	public void setDeployString(String deployString){this.deployString = deployString;}
	public String getPort(){return port;}
	public void setPort(String port){this.port = port;}
	public String getResultPath(){ return resultPath;}
	public void setResultPath(String resultPath){this.resultPath = resultPath;}

	@DataBoundConstructor
	public SeleniumGrailsBuilder(String browser, String baseDir, String grailsHome, String projectName, String deployString, String port, String resultPath){
		this.browser = browser;
		this.baseDir = baseDir;
		this.grailsHome = grailsHome;
		this.projectName = projectName;
    this.deployString = deployString;
		this.port = port;
		this.resultPath = resultPath;
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
    if(deployString.length() == 0){
			deployString = null;
		}
		if(port.length() == 0){
			port = "64381"; //default port, chosen slightly at random
		}

		//Map<String, String> env = build.getEnvVars();
		//env.put("GRAILS_HOME", grailsHome);
		FilePath resultFile =  null;		
		if(resultPath == null){				
			resultFile = new FilePath(new FilePath(build.getProject().getModuleRoot(), baseDir), "seleniumTestResult.xml");
		} else {
			resultFile = new FilePath(new File(resultPath));
		}
		listener.error("resultPath: "+resultPath);
		if(resultFile.exists()) resultFile.delete();
		Proc grailsServer = startGrails(deployString, grailsHome, port, build, launcher, listener);
		//String url = "http://localhost:"+port+"/"+projectName+"/selenium/core/TestRunner.html?test=..%2F..%2Fselenium/suite&auto=on&close=on&resultsUrl=..%2FpostResults%3Ffile%3DseleniumTestResult.xml";
		String url = "http://localhost:"+port+"/"+projectName+"/selenium/core/TestRunner.html?test=..%2F..%2Fselenium/suite&auto=on&close=on&resultsUrl=..%2FpostResults%3Ffile%3DseleniumTestResult.xml";

		String cmd = browser + " " + url;
		Proc browserProc = launcher.launch(cmd, build.getEnvVars(), listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));

		while(!resultFile.exists()){ Thread.sleep(1000); } //wait for the result file to be written

		browserProc.kill();
		
		if(grailsServer != null) grailsServer.kill();
		if(resultPath != null){ 
			//put the results somewhere where the publisher can find it
			System.out.println("moving "+resultPath+"to: "+new FilePath(new FilePath(build.getProject().getModuleRoot(), baseDir), "seleniumTestResult.xml").toString());
			new FilePath(new File(resultPath)).copyTo(new FilePath(new FilePath(build.getProject().getModuleRoot(), baseDir), "seleniumTestResult.xml"));
		}
		return true;
	}
	
	private Proc startGrails(String deployString, String grailsHome, String port, Build build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		Map<String, String> env = build.getEnvVars();
    env.put("GRAILS_HOME", grailsHome);
		if(deployString == null){
			String startGrailsCommand = String.format("%1$s/bin/grails -Dserver.port=%2$s run-app",grailsHome, port);
			Proc grailsServer = launcher.launch(startGrailsCommand, env, listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));
			Thread.sleep(60000);//give hte grails server some time to start up
			return grailsServer;
		} else {
			String buildCommand = String.format("%1$s/bin/grails war",grailsHome);
			Proc buildWar = launcher.launch(buildCommand, env, listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));
			buildWar.join();
			Proc deploy = launcher.launch(deployString, env, listener.getLogger(), new FilePath(build.getProject().getModuleRoot(), baseDir));
			deploy.join();
			return null;
		}
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
