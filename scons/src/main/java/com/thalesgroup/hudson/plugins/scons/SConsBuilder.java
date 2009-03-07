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

package com.thalesgroup.hudson.plugins.scons;
import hudson.CopyOnWrite;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.io.File;
import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;


/**
 * @author Gregory Boissinot
 */
public class SConsBuilder extends Builder {

	/**
	 * Identifies {@link SConsInstallation} to be used.
	 */
	private final String sconsName;

	private final String options;

	private final String variables;

	private final String targets;
	
	private final String rootSconsscriptDirectory;
	
	private final String sconsscript;

	@DataBoundConstructor
	public SConsBuilder(String sconsName, String options, String variables,
			String targets, String rootSconsscriptDirectory, String sconsscript) {
		this.sconsName = sconsName;
		this.options = options;
        this.variables = variables;
		this.targets = targets;
		this.rootSconsscriptDirectory=rootSconsscriptDirectory;
		this.sconsscript=sconsscript;
	}


	public String getSconsName() {
		return sconsName;
	}

	public String getOptions() {
		return options;
	}

	public String getVariables() {
		return variables;
	}

	public String getTargets() {
		return targets;
	}	

    public String getRootSconsscriptDirectory() {
		return rootSconsscriptDirectory;
	}


	public String getSconsscript() {
		return sconsscript;
	}


	public SConsInstallation getScons() {
        for( SConsInstallation i : DESCRIPTOR.getInstallations() ) {
            if(sconsName!=null && i.getName().equals(sconsName))
                return i;
        }
        return null;
    }	

	public boolean perform(Build<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException {
		Project proj = build.getProject();

		ArgumentListBuilder args = new ArgumentListBuilder();
		
        String execName;
        //Same invocation but can change
        if(launcher.isUnix())
            execName = "scons";
        else
            execName = "scons";
        
		SConsInstallation ai = getScons();
        if(ai==null) {
            args.add(execName);
        } else {
            File exec = ai.getExecutable();
            if(!ai.getExists()) {
                listener.fatalError(exec+" doesn't exist");
                return false;
            }
            args.add(exec.getPath());
        }		
	
		String normalizedOptions = options.replaceAll("[\t\r\n]+", " ");
		String normalizedSconsscript = sconsscript.replaceAll("[\t\r\n]+"," ");		
		String normalizedRootSconsscriptDirectory = rootSconsscriptDirectory.replaceAll("[\t\r\n]+"," ");	
		String normalizedFileVariables = variables.replaceAll("[\t\r\n]+", " ");
		String normalizedTargets = targets.replaceAll("[\t\r\n]+"," ");		
		
		
		if (normalizedOptions != null && normalizedOptions.trim().length() != 0) {
			args.addTokenized(normalizedOptions);
		}

		if (normalizedSconsscript != null && normalizedSconsscript.trim().length() != 0) {
			normalizedSconsscript=Util.replaceMacro(normalizedSconsscript, build.getEnvVars());
			args.add("-f");
			args.add(normalizedSconsscript);
		}
	
		if (normalizedRootSconsscriptDirectory != null && normalizedRootSconsscriptDirectory.trim().length() != 0) {
			normalizedRootSconsscriptDirectory=Util.replaceMacro(normalizedRootSconsscriptDirectory, build.getEnvVars());
			args.add("-C");
			args.add(normalizedRootSconsscriptDirectory);
		}		
				
		if (normalizedFileVariables != null && normalizedFileVariables.trim().length() != 0) {
			args.addTokenized(normalizedFileVariables);
		}		

		if (normalizedTargets != null && normalizedTargets.trim().length() != 0) {
			args.addTokenized(normalizedTargets);
		}			

		if (!launcher.isUnix()) {
			// on Windows, executing batch file can't return the correct error
			// code,
			// so we need to wrap it into cmd.exe.
			// double %% is needed because we want ERRORLEVEL to be expanded
			// after
			// batch file executed, not before. This alone shows how broken
			// Windows is...
			args.prepend("cmd.exe", "/C");
			args.add("&&", "exit", "%%ERRORLEVEL%%");
		}

		try {
			int r = launcher.launch(args.toCommandArray(), build.getEnvVars(),
					listener.getLogger(), proj.getModuleRoot()).join();
			return r == 0;
		} catch (IOException e) {
			Util.displayIOException(e, listener);
			e.printStackTrace(listener.fatalError("command execution failed"));
			return false;
		}
	}

	public Descriptor<Builder> getDescriptor() {
		return DESCRIPTOR;
	}

	public static final SConsBuilderDescriptor DESCRIPTOR = new SConsBuilderDescriptor();

	public static final class SConsBuilderDescriptor extends Descriptor<Builder> {

		@CopyOnWrite
		private volatile SConsInstallation[] installations = new SConsInstallation[0];

		private SConsBuilderDescriptor() {
			super(SConsBuilder.class);
			load();
		}


		public String getHelpFile() {
			return "/plugin/scons/SConsBuilder/help.html";
		}

		public String getDisplayName() {
			return "Invoke scons script";
		}

		public SConsInstallation[] getInstallations() {
			return installations;
		}

		public boolean configure(StaplerRequest req) {
			installations = req.bindParametersToList(SConsInstallation.class,"scons.").toArray(new SConsInstallation[0]);
			save();
			return true;
		}

	}

}
