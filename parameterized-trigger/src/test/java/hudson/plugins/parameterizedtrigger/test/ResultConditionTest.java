/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Tom Huybrechts
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
package hudson.plugins.parameterizedtrigger.test;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.PredefinedBuildParameters;
import hudson.plugins.parameterizedtrigger.ResultCondition;
import hudson.tasks.Builder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.HudsonTestCase;

public class ResultConditionTest extends HudsonTestCase {

    public void testTriggerByStableBuild() throws Exception {
        Project projectA = createFreeStyleProject("projectA");
        Project projectB = createFreeStyleProject("projectB");

        schedule(projectA, ResultCondition.SUCCESS);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.FAILED);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.UNSTABLE_OR_BETTER);
        Assert.assertEquals(2, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.UNSTABLE);
        Assert.assertEquals(2, projectB.getLastBuild().getNumber());
    }

    public void testTriggerByUnstableBuild() throws Exception {
        Project projectA = createFreeStyleProject("projectA");
        projectA.getBuildersList().add(new UnstableBuilder());
        Project projectB = createFreeStyleProject("projectB");

		schedule(projectA, ResultCondition.SUCCESS);
        Assert.assertNull(projectB.getLastBuild());

        schedule(projectA, ResultCondition.FAILED);
        Assert.assertNull(projectB.getLastBuild());

        schedule(projectA, ResultCondition.UNSTABLE_OR_BETTER);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.UNSTABLE);
        Assert.assertEquals(2, projectB.getLastBuild().getNumber());
}

	private void schedule(Project projectA, ResultCondition condition)
			throws IOException, InterruptedException, ExecutionException {
		projectA.getPublishersList().add(new BuildTrigger(new BuildTriggerConfig("projectB", condition, new PredefinedBuildParameters(""))));
        projectA.scheduleBuild2(0).get();
        Thread.sleep(500);
	}

    public void testTriggerByFailedBuild() throws Exception {
        Project projectA = createFreeStyleProject("projectA");
        projectA.getBuildersList().add(new FailureBuilder());
        Project projectB = createFreeStyleProject("projectB");

		schedule(projectA, ResultCondition.SUCCESS);
        Assert.assertNull(projectB.getLastBuild());

        schedule(projectA, ResultCondition.FAILED);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.UNSTABLE_OR_BETTER);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());

        schedule(projectA, ResultCondition.UNSTABLE);
        Assert.assertEquals(1, projectB.getLastBuild().getNumber());
    }

    public static class UnstableBuilder extends Builder {

        public Descriptor<Builder> getDescriptor() {
            throw new UnsupportedOperationException();
        }

        @Override
         public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener buildListener) throws InterruptedException, IOException {
            build.setResult(Result.UNSTABLE);
            return true;
        }
    }

}
