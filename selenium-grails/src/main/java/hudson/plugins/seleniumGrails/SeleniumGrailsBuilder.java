package hudson.plugins.seleniumGrails;

import hudson.tasks.Builder;
import hudson.model.Result;
import hudson.model.Descriptor;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import org.kohsuke.stapler.DataBoundConstructor;

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

	@Override
	public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
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
			resultFile = new FilePath(new FilePath(build.getModuleRoot(), baseDir), "seleniumTestResult.xml");
		} else {
			resultFile = new FilePath(new File(resultPath));
		}
		listener.error("resultPath: "+resultPath);
		if(resultFile.exists()) resultFile.delete();
		Proc grailsServer = startGrails(deployString, grailsHome, port, build, launcher, listener);
		//String url = "http://localhost:"+port+"/"+projectName+"/selenium/core/TestRunner.html?test=..%2F..%2Fselenium/suite&auto=on&close=on&resultsUrl=..%2FpostResults%3Ffile%3DseleniumTestResult.xml";
		String url = "http://localhost:"+port+"/"+projectName+"/selenium/core/TestRunner.html?test=..%2F..%2Fselenium/suite&auto=on&close=on&resultsUrl=..%2FpostResults%3Ffile%3DseleniumTestResult.xml";

		Proc browserProc = launcher.launch().cmds(browser, url).envs(build.getEnvironment(listener)).stdout(listener).pwd(new FilePath(build.getModuleRoot(), baseDir)).start();

		while(!resultFile.exists()){ Thread.sleep(1000); } //wait for the result file to be written

		browserProc.kill();
		
		if(grailsServer != null) grailsServer.kill();
		if(resultPath != null){ 
			//put the results somewhere where the publisher can find it
			System.out.println("moving "+resultPath+"to: "+new FilePath(new FilePath(build.getModuleRoot(), baseDir), "seleniumTestResult.xml"));
			new FilePath(new File(resultPath)).copyTo(new FilePath(new FilePath(build.getModuleRoot(), baseDir), "seleniumTestResult.xml"));
		}
		return true;
	}
	
	private Proc startGrails(String deployString, String grailsHome, String port, AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		Map<String, String> env = build.getEnvironment(listener);
    env.put("GRAILS_HOME", grailsHome);
		if(deployString == null){
			Proc grailsServer = launcher.launch().cmds(grailsHome + "/bin/grails", "-Dserver.port=" + port, "run-app").envs(env).stdout(listener).pwd(new FilePath(build.getModuleRoot(), baseDir)).start();
			Thread.sleep(60000);//give hte grails server some time to start up
			return grailsServer;
		} else {
			Proc buildWar = launcher.launch().cmds(grailsHome + "/bin/grails", "war").envs(env).stdout(listener).pwd(new FilePath(build.getModuleRoot(), baseDir)).start();
			buildWar.join();
                        //XXX do we need to split deployString into cmd/args?
			Proc deploy = launcher.launch().cmds(deployString).envs(env).stdout(listener).pwd(new FilePath(build.getModuleRoot(), baseDir)).start();
			deploy.join();
			return null;
		}
	}

    @Extension
    public static final class DescriptorImpl extends Descriptor<Builder> {
        public DescriptorImpl() {
            super(SeleniumGrailsBuilder.class);
        }

        public String getDisplayName() {
            return "Selenium grails";
        }
    }
}
