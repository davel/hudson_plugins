/*******************************************************************************
* Copyright (c) 2009 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot                                                   *
*                                                                              *
* Permission is hereby granted, free of charge, to any person obtaining a copy *
* of this software and associated documentation files (the "Software"), to deal*
* in the Software without restriction, including without limitation the rights *
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
* copies of the Software, and to permit persons to whom the Software is        *
* furnished to do so, subject to the following conditions:                     *
*                                                                              *
* The above copyright notice and this permission notice shall be included in   *
* all copies or substantial portions of the Software.                          *
*                                                                              *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
* THE SOFTWARE.                                                                *
*******************************************************************************/

package com.thalesgroup.hudson.plugins.copyarchiver;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.maven.AbstractMavenProject;
import hudson.maven.MavenBuild;
import hudson.maven.MavenModule;
import hudson.maven.MavenModuleSetBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.Result;
import hudson.model.Run;
import hudson.tasks.Publisher;
import hudson.util.FormFieldValidator;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * 
 * @author Gregory Boissinot
 */
public class CopyArchiver extends Publisher implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final CopyArchiverDescriptor DESCRIPTOR = new CopyArchiverDescriptor();
		
	private String sharedDirectoryPath;
	
	private boolean useTimestamp;
	
	private String datePattern;
	
	private boolean flatten;
	
    private final List<ArchivedJobEntry> archivedJobList   = new ArrayList<ArchivedJobEntry>();	
	    
    public String getSharedDirectoryPath() {
		return sharedDirectoryPath;
	}

	public void setSharedDirectoryPath(String sharedDirectoryPath) {
		this.sharedDirectoryPath = sharedDirectoryPath;
	}

	public boolean getUseTimestamp() {
		return useTimestamp;
	}

	public boolean getFlatten() {
		return flatten;
	}
	
	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
	}	
	
	public void setUseTimestamp(boolean useTimestamp) {
		this.useTimestamp = useTimestamp;
	}	

	public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }

	public List<ArchivedJobEntry> getArchivedJobList() {
		return archivedJobList;
	}    
        
	public String getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
		
    public static final class CopyArchiverDescriptor extends Descriptor<Publisher>{

    	//CopyOnWriteList
    	private List<AbstractProject> jobs;
    	
        public CopyArchiverDescriptor() {
            super(CopyArchiver.class);
        }

        @Override
        public String getDisplayName() {
            return "Aggregate the archived artifacts";
        }

        @Override
        public Publisher newInstance(StaplerRequest req) throws FormException {
            CopyArchiver pub = new CopyArchiver();
            req.bindParameters(pub, "copyarchiver.");
            pub.getArchivedJobList().addAll(req.bindParametersToList(ArchivedJobEntry.class, "copyarchiver.entry."));
            return pub;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/copyarchiver/help.html";
        }
        
        
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        	return true;	
        }

		public List<AbstractProject> getJobs() {
			 return Hudson.getInstance().getItems(AbstractProject.class);
		}      
		
		public void doDateTimePatternCheck(final StaplerRequest req,
				StaplerResponse rsp) throws IOException, ServletException {
			(new FormFieldValidator(req, rsp, true) {

				public void check() throws IOException, ServletException {

					String pattern = req.getParameter("value");

					if (pattern == null || pattern.trim().length() == 0) {
						error((new StringBuilder()).append(
								"You must provide a pattern value").toString());
					}

					try {
						new SimpleDateFormat(pattern);
					} catch (NullPointerException npe) {
						error((new StringBuilder()).append("Invalid input: ")
								.append(npe.getMessage()).toString());
						return;
					} catch (IllegalArgumentException iae) {
						error((new StringBuilder()).append("Invalid input: ")
								.append(iae.getMessage()).toString());
						return;
					}

					return;

				}
			}).process();
		}		
                      
    }


    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    } 
    
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean perform(Build<?,?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
    	
    	try{
    		
    		Project project = build.getProject();
    		
    		if (build.getResult().equals(Result.UNSTABLE) || build.getResult().equals(Result.SUCCESS)){
    	    		
    			listener.getLogger().println("Starting copy archived artifacts in the shared directory.");
    			
    			File destDir = null;
    			
    			//Use a temporary map in read/write access
    			Map vars = new HashMap();
    			vars.putAll(build.getEnvironment(listener).descendingMap());
    			
    			
    			if (useTimestamp){
    				
    				if (datePattern==null || datePattern.trim().isEmpty()){
    					build.setResult(Result.FAILURE);
    					throw new AbortException("The option 'Change the date format' is activated. You must provide a new date pattern.");    					
    				}
    				
    		        SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
    		        final String newBuildIdStr = sdf.format(build.getTimestamp().getTime());         			
        			vars.put("BUILD_ID", newBuildIdStr);
    			}

    			//Replace environment variables
    			String sharedDirectoryPathParsed = Util.replaceMacro(sharedDirectoryPath,  vars);
    			//Replace build variables
    			sharedDirectoryPathParsed = Util.replaceMacro(sharedDirectoryPathParsed,  build.getBuildVariables());  
    			
    			
    			destDir = new File(sharedDirectoryPathParsed);
    			listener.getLogger().println("Copying archived artifacts in the shared directory '" + destDir + "'.");    		    			
    			deleteDir(destDir);    			
    			destDir.mkdirs();
    			FilePath destDirFilePath = new FilePath(destDir);
    			
    			FilePath lastSuccessfulDirFilePath = null;
    			FilePathArchiver lastSuccessfulDirFilePathArchiver=null;
    			int numCopied=0;
    			for (ArchivedJobEntry archivedJobEntry:archivedJobList){    		    				
    				AbstractProject curProj = Project.findNearest(archivedJobEntry.jobName);
    				Run run = curProj.getLastSuccessfulBuild();
    				if (run!=null){    				

						//if the selected project is the current projet, we're using the workspace base directory or SCM module root    						
						if (project.getName().equals(archivedJobEntry.jobName)){
    						lastSuccessfulDirFilePath = project.getModuleRoot();
    						lastSuccessfulDirFilePathArchiver=new FilePathArchiver(lastSuccessfulDirFilePath);
    						numCopied+=lastSuccessfulDirFilePathArchiver.copyRecursiveTo(flatten,archivedJobEntry.pattern, archivedJobEntry.excludes, destDirFilePath);
						}
    					
    					//if the selected project is not the current project, we are 2 cases: Maven project and not a Maven project
						
						// Maven projects
						else if (curProj instanceof AbstractMavenProject){
	    					MavenModuleSetBuild vMavenModuleSetBuild =(MavenModuleSetBuild)run;
	    					Map<MavenModule,List<MavenBuild>> moduleBuildsMap = vMavenModuleSetBuild.getModuleBuilds();
	    					for (MavenModule mm:moduleBuildsMap.keySet()){
	    						File lastSuccessfulDir = mm.getLastSuccessfulBuild().getArtifactsDir();
	        					lastSuccessfulDirFilePath = new FilePath(lastSuccessfulDir);
	        					lastSuccessfulDirFilePathArchiver=new FilePathArchiver(lastSuccessfulDirFilePath);
	        					numCopied+=lastSuccessfulDirFilePathArchiver.copyRecursiveTo(flatten,archivedJobEntry.pattern, archivedJobEntry.excludes,destDirFilePath);
	    					}    			
    					}
						
						// non Maven projects
						else{
    						File lastSuccessfulDir = run.getArtifactsDir();
        					lastSuccessfulDirFilePath = new FilePath(lastSuccessfulDir);
    						lastSuccessfulDirFilePathArchiver=new FilePathArchiver(lastSuccessfulDirFilePath);    						    						
    						numCopied+=lastSuccessfulDirFilePathArchiver.copyRecursiveTo(flatten,archivedJobEntry.pattern, archivedJobEntry.excludes, destDirFilePath);
    					}
    				}    				    				
    			}
    			listener.getLogger().println("'"+numCopied+"' artifacts have been copied.");
    			
    			listener.getLogger().println("Stop copying archived artifacts in the shared directory.");    
    		}    
        } 
    	catch (Exception e) {
            e.printStackTrace(listener.fatalError("error"));
            build.setResult(Result.FAILURE);
            return true;
        }        
            
		return true;
	}
  
}
