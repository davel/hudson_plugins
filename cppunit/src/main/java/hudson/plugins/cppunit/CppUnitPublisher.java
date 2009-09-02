package hudson.plugins.cppunit;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.cppunit.util.Messages;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.transform.TransformerException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class that records CppUnit test reports into Hudson.
 * 
 * @author Gregory Boissinot
 *   
 */
public class CppUnitPublisher extends hudson.tasks.Publisher implements Serializable {

    private static final long serialVersionUID = 1L;

    @Extension
    public static final CppUnitDescriptor DESCRIPTOR = new CppUnitDescriptor();

    private String testResultsPattern = null;
    
    private boolean useWorkspaceBaseDir = false;

	public String getTestResultsPattern() {
		return testResultsPattern;
	}

	public void setTestResultsPattern(String testResultsPattern) {
		this.testResultsPattern = testResultsPattern;
	}

	public boolean isUseWorkspaceBaseDir() {
		return useWorkspaceBaseDir;
	}

	public void setUseWorkspaceBaseDir(boolean useWorkspaceBaseDir) {
		this.useWorkspaceBaseDir = useWorkspaceBaseDir;
	}

	@Override
    public Action getProjectAction(hudson.model.Project project) {
         return new TestResultProjectAction(project);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
    	
    	boolean recordingResult = false;
        try {
        	   	
        	Messages.log(listener,"Recording of the CppUnit tests results.");
                        
            //Build the transformer
            CppUnitTransformer cppUnitTransformer;            
            cppUnitTransformer = new CppUnitTransformer();
            
    		//Create the temporary target junit dir
    		FilePath junitTargetFilePath = new FilePath(build.getProject().getWorkspace(),"cppunitpluginTemp");
            if (junitTargetFilePath.exists()) {
            	junitTargetFilePath.deleteRecursive();
            }
            junitTargetFilePath.mkdirs();            
            
            //Compute the basedir
            FilePath basedir = null;
            if (useWorkspaceBaseDir){
            	basedir=build.getWorkspace();
            }
            else{
                FilePath[] moduleRoots= build.getModuleRoots();
                boolean multipleModuleRoots= moduleRoots != null && moduleRoots.length > 1;
                basedir= multipleModuleRoots ? build.getWorkspace() : build.getModuleRoot();	
            }            
            
            // Archiving CppunitFile into Junit files
            CppUnitArchiver archiver = new CppUnitArchiver(listener, junitTargetFilePath, testResultsPattern, cppUnitTransformer);
            Result result = basedir.act(archiver);
            
            // set the build status to SUCCESSFUL or UNSTABLE
            build.setResult(result);
            
            //Recording the tests ( the build status can change)
            recordingResult = recordTestResult(build, basedir, listener, junitTargetFilePath, "TEST-*.xml");            
            
            //Detroy temporary target junit dir           
            junitTargetFilePath.deleteRecursive();
            
        } 
        catch (TransformerException te) {
        	Messages.log(listener,"[Error]- Recording of the CppUnit tests results. " + te.toString());
        	build.setResult(Result.FAILURE);            
        }

         
        Messages.log(listener,"End recording of the CppUnit tests results.");        
        return recordingResult;
    }

 

    @Override
    public CppUnitDescriptor getDescriptor() {
        return DESCRIPTOR;        
    }
    


    public static final class CppUnitDescriptor extends BuildStepDescriptor<Publisher> {

        public CppUnitDescriptor() {
            super(CppUnitPublisher.class);
            load();	          
        }

        @Override
        public String getDisplayName() {
            return "Publish CppUnit test result report";
        }
        
        @Override
        public boolean isApplicable(Class type) {
            return true;
        }        

        @Override
        public String getHelpFile() {
            return "/plugin/cppunit/help.html";
        }

        @Override
        public Publisher newInstance(StaplerRequest req) throws FormException {
        	CppUnitPublisher pub= new CppUnitPublisher();
        	req.bindParameters(pub,"cppunit_reports.");
        	return pub;
        }                
    }
    
    /**
     * Record the test results into the current build.
     * @param build the cuurent build
     * @param listener the current listener
     * @param junitTargetFilePath the dest junit directory
     * @param junitFilePattern the junit pattern
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(final AbstractBuild<?,?> build, 
    								 final FilePath baseDirTestResult,
    								 final BuildListener listener, 
    								 final FilePath junitTargetFilePath, 
    								 final String junitFilePattern)
            throws InterruptedException, IOException {
    	
    	
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();

            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitTargetFilePath, junitFilePattern, baseDirTestResult, existingTestResults, buildTime);

            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }
            
            if(result.getPassCount()==0 && result.getFailCount()==0){
            	throw new AbortException("None of the test reports contained any result");
            }
                
        } catch (AbortException e) {
            if(build.getResult()==Result.FAILURE)
                // most likely a build failed before it gets to the test phase.
                // don't report confusing error message.
                return true;

            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        if (existingAction == null) {
        	build.getActions().add(action);
        }

        if(action.getResult().getFailCount()>0)
        	build.setResult(Result.UNSTABLE);

        return true;
    }

    /**
     * Collect the test results from the files
     * @param junitFilePattern
     * @param baseDirTestResult
     * @param existingTestResults existing test results to add results to
     * @param buildTime
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult(final FilePath temporaryJunitFilePath, 
    								 final String junitFilePattern,
    								 final FilePath baseDirTestResult,
    								 final TestResult existingTestResults, 
    								 final long buildTime) 
    				throws IOException, InterruptedException {
    	
    	
    	final File temporaryJunitDirFile = new File(temporaryJunitFilePath.toURI());
    	
        TestResult result = baseDirTestResult.act(new FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
            	
                FileSet fs = Util.createFileSet(temporaryJunitDirFile, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();
                if(files.length==0) {
                    // no test result. Most likely a configuration error or fatal problem
                    throw new AbortException("No test report files were found. Configuration error?");
                }
                if (existingTestResults == null) {
                    return new TestResult(buildTime, ds);
                } else {
                    existingTestResults.parse(buildTime, ds);
                    return existingTestResults;
                }
            }
        });
        return result;
    }

	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}        
}
