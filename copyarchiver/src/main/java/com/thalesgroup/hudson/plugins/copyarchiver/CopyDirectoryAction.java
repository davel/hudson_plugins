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

import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class CopyDirectoryAction implements FilePath.FileCallable<Boolean>, Serializable{


	
	private static final long serialVersionUID = 1L;
	
    private static final Pattern DRIVE_PATTERN = Pattern.compile("[A-Za-z]:\\\\.+");
	
    private static final Logger LOGGER = Logger.getLogger(CopyDirectoryAction.class.getName());
	
   
    private String sharedPath;

    
    public CopyDirectoryAction(String sharedPath){
    	this.sharedPath=sharedPath;
    }
    
    public Boolean invoke(java.io.File workspace, VirtualChannel channel) throws IOException {
    	        	
    	return true;
    }


 
    
}
