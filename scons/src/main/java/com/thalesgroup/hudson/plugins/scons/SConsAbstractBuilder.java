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

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;


public abstract class SConsAbstractBuilder extends Builder {


    private final String sconsName;

    private final String options;

    private final String variables;

    private final String targets;

    private final String rootSconsscriptDirectory;

    protected SConsAbstractBuilder(String sconsName, String options, String variables, String targets, String rootSconsscriptDirectory) {
        this.sconsName = sconsName;
        this.options = options;
        this.variables = variables;
        this.targets = targets;
        this.rootSconsscriptDirectory = rootSconsscriptDirectory;
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

    protected boolean buildSconsExecutable(Launcher launcher, BuildListener listener, ArgumentListBuilder args, EnvVars env) throws IOException, InterruptedException {
        SConsInstallation sconsInstallation = getSconsInstallation();
        if (sconsInstallation == null) {
            args.add("scons");
        } else {
            sconsInstallation = sconsInstallation.forNode(Computer.currentComputer().getNode(), listener);
            sconsInstallation = sconsInstallation.forEnvironment(env);

            String sconsExecutable = sconsInstallation.getSconsExecutable(launcher);
            if (sconsExecutable == null) {
                listener.fatalError(Messages.scons_NoSconsExecutable());
                return true;
            }
            args.add(sconsExecutable);
        }
        return false;
    }


    protected SConsInstallation getSconsInstallation() {
        for (SConsInstallation installation : getDescritor().getInstallations()) {
            if (getSconsName() != null && installation.getName().equals(getSconsName())) {
                return installation;
            }
        }
        return null;
    }

    public abstract SConsBuilderDescriptor getDescritor();
}

