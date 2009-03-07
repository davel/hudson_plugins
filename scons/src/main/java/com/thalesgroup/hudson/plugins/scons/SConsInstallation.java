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

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

/**
 * SCons installation.
 * 
 * @author Gregory Boissinot
*/
public final class SConsInstallation {
	
	
    private final String name;
    private final String executablePath;

    @DataBoundConstructor
    public SConsInstallation(String name, String executablePath) {
        this.name = name;
        this.executablePath = executablePath;
    }

    
	public String getExecutablePath() {
		return executablePath;
	}

	/**
     * Human readable display name.
     */
    public String getName() {
        return name;
    }

    public File getExecutable() {
        return new File(getExecutablePath());
    }
    
    /**
     * Returns true if the executable exists.
     */
    public boolean getExists() {
        return getExecutable().exists();
    }
}
