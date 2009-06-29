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
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Copy;

public class FilePathArchiver implements Serializable{

	
	FilePath filePath;
	
	public FilePathArchiver(FilePath filePath){
		this.filePath=filePath;
	}
	
    public int copyRecursiveTo(final boolean flatten, final String fileMask, final String excludes, final FilePath target) throws IOException, InterruptedException {
        if(filePath.getChannel()==target.getChannel()) {
            // local to local copy.
            return filePath.act(new FileCallable<Integer>() {
                public Integer invoke(File base, VirtualChannel channel) throws IOException {
                    if(!base.exists())  return 0;
                    assert target.getChannel()==null;

                    try {
                        class CopyImpl extends Copy {
                            private int copySize;

                            public CopyImpl() {
                                setProject(new org.apache.tools.ant.Project());
                            }

                            protected void doFileOperations() {
                                copySize = super.fileCopyMap.size();
                                super.doFileOperations();
                            }

                            public int getNumCopied() {
                                return copySize;
                            }
                        }

                        CopyImpl copyTask = new CopyImpl();
                        copyTask.setTodir(new File(target.getRemote()));
                        copyTask.addFileset(Util.createFileSet(base,fileMask,excludes));
                        copyTask.setIncludeEmptyDirs(false);
                        copyTask.setFlatten(flatten);
                        
                        copyTask.execute();
                        return copyTask.getNumCopied();
                    } catch (BuildException e) {
                        throw new IOException2("Failed to copy "+base+"/"+fileMask+" to "+target,e);
                    }
                }
            });
        } else{
        	return filePath.copyRecursiveTo(fileMask, excludes, target);
        }
    }	
}
