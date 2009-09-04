/**
 * The MIT License
 *
 * Copyright (c) 2007-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Erik Ramfelt,
 *                          Henrik Lynggaard, Peter Liljenberg, Andrew Bayer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.clearcase; 

import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.util.VariableResolver;
import hudson.plugins.clearcase.base.BaseChangeLogAction;
import hudson.plugins.clearcase.base.BaseHistoryAction;
import hudson.plugins.clearcase.history.HistoryAction;
import hudson.plugins.clearcase.util.BuildVariableResolver;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;

public class ClearCaseSCMTest extends AbstractWorkspaceTest {

    private Mockery classContext;
    private Mockery context;
    private ClearTool cleartool;
    private AbstractProject project;
    private Build build;
    private Launcher launcher;
    private ClearToolLauncher clearToolLauncher;
    private ClearCaseSCM.ClearCaseScmDescriptor clearCaseScmDescriptor;
    
    @Before
    public void setUp() throws Exception {
        createWorkspace();
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        project = classContext.mock(AbstractProject.class);
        build = classContext.mock(Build.class);
        launcher = classContext.mock(Launcher.class);
	clearCaseScmDescriptor = classContext.mock(ClearCaseSCM.ClearCaseScmDescriptor.class);
	context = new Mockery();
	cleartool = context.mock(ClearTool.class);
	clearToolLauncher = context.mock(ClearToolLauncher.class);
	
    }
    @After
    public void tearDown() throws Exception {
        deleteWorkspace();
    }
    
    @Test
    public void testCreateChangeLogParser() {
        AbstractClearCaseScm scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", false, "", null, false, false, false);
        assertNotNull("The change log parser is null", scm.createChangeLogParser());
        assertNotSame("The change log parser is re-used", scm.createChangeLogParser(), scm.createChangeLogParser());
    }
    
    @Test
    public void testSnapshotBuildEnvVars() {
        classContext.checking(new Expectations() {
            {
                ignoring(build).getParent(); will(returnValue(project));
            }
        });
        AbstractClearCaseScm scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", false, "", null, false, false,false);
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/hudson/jobs/job/workspace");
        scm.generateNormalizedViewName(build, launcher);
        scm.buildEnvVars(build, env);
        assertEquals("The env var VIEWNAME wasnt set", "viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertEquals("The env var VIEWPATH wasnt set", "/hudson/jobs/job/workspace" + File.separator +"viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }

    @Test
    public void testDynamicBuildEnvVars() {
        classContext.checking(new Expectations() {
            {
                ignoring(build).getParent(); will(returnValue(project));
            }
        });
        AbstractClearCaseScm scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", true, "/views", null, false, false,false);
        Map<String, String> env = new HashMap<String, String>();
        scm.generateNormalizedViewName(build, launcher);
        scm.buildEnvVars(build, env);
        assertEquals("The env var VIEWNAME wasnt set", "viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertEquals("The env var VIEWPATH wasnt set", "/views" + File.separator +"viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }

    @Test
    public void testDynamicBuildEnvVarsNoViewDrive() {
        classContext.checking(new Expectations() {
            {
                ignoring(build).getParent(); will(returnValue(project));
            }
        });
        AbstractClearCaseScm scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", true, null, null, false, false,false);
        Map<String, String> env = new HashMap<String, String>();
        scm.generateNormalizedViewName(build, launcher);
        scm.buildEnvVars(build, env);
        assertEquals("The env var VIEWNAME wasnt set", "viewname", env.get(AbstractClearCaseScm.CLEARCASE_VIEWNAME_ENVSTR));
        assertFalse("The env var VIEWPATH was set", env.containsKey(AbstractClearCaseScm.CLEARCASE_VIEWPATH_ENVSTR));
    }

    @Test
    public void testGetBranch() {
        ClearCaseSCM scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", false, "", null, false, false,false);
        assertEquals("The branch isnt correct", "branch", scm.getBranch());
    }
    
    @Test
    public void testGetConfigSpec() {
        ClearCaseSCM scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", false, "", null, false, false,false);
        assertEquals("The config spec isnt correct", "configspec", scm.getConfigSpec());
    }

    @Test
    public void testIsUseUpdate() {
        AbstractClearCaseScm scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", false, "", null, false, false,false);
        assertTrue("The isUpdate isnt correct", scm.isUseUpdate());
        
        scm = new ClearCaseSCM("branch", "configspec", "viewname", false, "", false, "", null, false, false,false);
        assertFalse("The isUpdate isnt correct", scm.isUseUpdate());
    }

    @Test
    public void testIsDynamicView() {
        ClearCaseSCM scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", true, "", null, false, false,false);
        assertTrue("The dynamic isnt correct", scm.isUseDynamicView());
        assertFalse("The use update isnt correct", scm.isUseUpdate());
    }

    @Test
    public void testGetViewDrive() {
        ClearCaseSCM scm = new ClearCaseSCM("branch", "configspec", "viewname", true, "", true, "/tmp/c", null, false, false,false);
        assertEquals("The view drive isnt correct", "/tmp/c", scm.getViewDrive());
    }

    @Test
    public void testGetBranchNames() {
        AbstractClearCaseScm scm = new ClearCaseSCM("branchone branchtwo", "configspec", "viewname", true, "", true, "/tmp/c", null, false, false,false);
        assertArrayEquals("The branch name array is incorrect", new String[]{"branchone", "branchtwo"}, scm.getBranchNames());
    }

    @Test
    public void assertEmptyBranchIsReturnedAsABranch() {
        AbstractClearCaseScm scm = new ClearCaseSCM("", "configspec", "viewname", true, "", true, "/tmp/c", null, false, false,false);
        assertArrayEquals("The branch name array is incorrect", new String[]{""}, scm.getBranchNames());
    }

    @Test
    public void assertBranchWithSpaceWorks() {
        AbstractClearCaseScm scm = new ClearCaseSCM("branch\\ one", "configspec", "viewname", true, "", true, "/tmp/c", null, false, false,false);
        assertArrayEquals("The branch name array is incorrect", new String[]{"branch one"}, scm.getBranchNames());
    }

    @Test
    public void testGetViewPaths() throws Exception {
        AbstractClearCaseScm scm = new ClearCaseSCM("branchone branchtwo", "configspec", "viewname", true, "load tmp", true, "", null, false, false,false);
        assertEquals("The view paths string is incorrect", "tmp", scm.getViewPaths()[0]);
    }

//    @Test
//    public void assertExtendedViewPathIsSetForDynamicViews() throws Exception {
//        classContext.checking(new Expectations() {
//            {
//                ignoring(build).getParent(); will(returnValue(project));
//            }
//        });
//        ClearCaseSCM scm = new ClearCaseSCM("branchone", "configspec", "viewname", true, "vob", true, "/view", null, false, false);
//        BaseHistoryAction action = (BaseHistoryAction) scm.createHistoryAction(null, null);
//        assertEquals("The extended view path is incorrect", "/view/viewname", action.getExtendedViewPath());
//    }

    @Test
    public void assertExtendedViewPathUsesNormalizedViewName() throws Exception {
        classContext.checking(new Expectations() {
            {
                atLeast(2).of(build).getParent(); will(returnValue(project));
                one(project).getName(); will(returnValue("ClearCase"));
		atLeast(1).of(clearCaseScmDescriptor).getLogMergeTimeWindow(); will(returnValue(5));
	    }
	    });
	context.checking(new Expectations() {
		{
		    one(cleartool).pwv("viewname-ClearCase");
		    will(returnValue("/view/viewname-ClearCase"));
		}
	    });
	
        ClearCaseSCM scm = new ClearCaseSCMDummy("branchone", "configspec", "viewname-${JOB_NAME}", true, "vob",
					    true, "/view", null, false, false, false, null);
	// Create actions
	VariableResolver variableResolver = new BuildVariableResolver(build,
								      launcher);
        BaseHistoryAction action = (BaseHistoryAction) scm.createHistoryAction(variableResolver, clearToolLauncher);
        assertEquals("The extended view path is incorrect", "/view/viewname-clearcase/", action.getExtendedViewPath());
	classContext.assertIsSatisfied();
    }
    
    private void assertObjectInArray(Object[] array, Object obj) {
        boolean found = false;
        for (Object objInArray : array) {
            if (obj.equals(objInArray)) {
                found = true;
            }
        }
        if (!found) {
            fail(obj + " was not found in array " + Arrays.toString(array));
        }
    }

    private class ClearCaseSCMDummy extends ClearCaseSCM {
	public ClearCaseSCMDummy(String branch, String configspec, String viewname,
				 boolean useupdate, String loadRules, boolean usedynamicview,
				 String viewdrive, String mkviewoptionalparam,
				 boolean filterOutDestroySubBranchEvent,
				 boolean doNotUpdateConfigSpec, boolean rmviewonrename,
				 String excludedRegions) {
	    super(branch, configspec, viewname, useupdate, loadRules, usedynamicview,
		  viewdrive, mkviewoptionalparam, filterOutDestroySubBranchEvent, doNotUpdateConfigSpec,
		  rmviewonrename, excludedRegions);
	}
    
	@Override
	protected ClearTool createClearTool(VariableResolver variableResolver,
					    ClearToolLauncher launcher) {
	    return cleartool;
	}

	@Override
	public ClearCaseScmDescriptor getDescriptor() {
	    return clearCaseScmDescriptor;
	}
    }
	
}
