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

import hudson.model.AbstractProject;

import java.io.Serializable;

public class ArchivedJobEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The job name
     */
    public transient String jobName;

    /**
     * The job object
     */
    public AbstractProject job;

    /**
     * Pattern to filtering the archived artifact to copy
     */
    public String pattern;

    /**
     * Pattern for excluding some archived artifact to copy
     */
    public String excludes;

    //Keep backward compatibility with copyarchiver 0.4.2 and less.
    //Can't use read the usually readResolve() method because all projects  haven't been initialized (within current project)
    @SuppressWarnings("unused")
    public String getJobStrName() {
        if (job != null) {
            return job.getName();
        } else {
            return jobName;
        }
    }
}
