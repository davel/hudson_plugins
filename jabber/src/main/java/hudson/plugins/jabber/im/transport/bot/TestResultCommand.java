package hudson.plugins.jabber.im.transport.bot;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.test.AbstractTestResultAction;

import java.util.List;

/**
 * Print out the latest test results for a build
 * @author R. Tyler Ballance <tyler@slide.com>
 */
public class TestResultCommand extends AbstractMultipleJobCommand {
	
    @Override
    protected String getCommandShortName() {
        return "test results";
    }

    @Override
    protected CharSequence getMessageForJob(AbstractProject<?, ?> job) {
        AbstractBuild<?, ?> build = job.getLastCompletedBuild();
        if (build == null) {
            // No builds
            return job.getName() + " has never been built";
        }   
        AbstractTestResultAction<?> tests = build.getTestResultAction();
        if (tests == null) {
            // no test results associated with this job
            return job.getName() + ": latest build contains no test results";
        }
        StringBuilder listing = new StringBuilder(String.format("%s build #%s had %s of %s tests fail\n", job.getName(), build.getNumber(), tests.getFailCount(), tests.getTotalCount()));
        
        listing.append("\n");
        List<CaseResult> failedTests = tests.getFailedTests();
        for (CaseResult result : failedTests) {
            listing.append(String.format("%s failed in %ss\n", result.getFullName(), result.getDuration()));
        }
        return listing;
    }

}
