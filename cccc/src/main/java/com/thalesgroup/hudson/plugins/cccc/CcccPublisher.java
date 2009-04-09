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

package com.thalesgroup.hudson.plugins.cccc;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;



public class CcccPublisher extends Publisher implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final CcccDescriptor DESCRIPTOR = new CcccDescriptor();

    private final String metricFilePath;
    
    public Descriptor<Publisher> getDescriptor() {
        return DESCRIPTOR;
    }
    
    @DataBoundConstructor
    public CcccPublisher(String metricFilePath){
        this.metricFilePath = metricFilePath;
    }

    @Override
    public Action getProjectAction(AbstractProject<?,?> project){
        return new CcccProjectAction(project);
    }

    protected boolean canContinue(final Result result) {
        return result != Result.ABORTED && result != Result.FAILURE;
    }

    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener){

    	listener.getLogger().println("Parsing cccc results");
    	
        if(this.canContinue(build.getResult())){
            FilePath workspace = build.getProject().getWorkspace();
            PrintStream logger = listener.getLogger();
            CccccParser parser = new CccccParser(new FilePath(build.getParent().getWorkspace(), metricFilePath),  logger);
            
            CcccReport report;
            try{
                report = workspace.act(parser);
            
            }catch(IOException ioe){
                ioe.printStackTrace(logger);
                build.setResult(Result.FAILURE);
                return false;
            
            }catch(InterruptedException ie){
                ie.printStackTrace(logger);
                build.setResult(Result.FAILURE);
                return false;
            }

            CcccResult result = new CcccResult(report, build);
            CcccBuildAction buildAction = new CcccBuildAction(build, result);
            build.addAction(buildAction);
        }
        return true;
    }

	public String getMetricFilePath() {
		return metricFilePath;
	}


    
}
