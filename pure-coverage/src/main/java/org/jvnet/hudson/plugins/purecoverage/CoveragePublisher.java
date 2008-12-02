package org.jvnet.hudson.plugins.purecoverage;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Project;
import hudson.model.Result;
import hudson.tasks.Publisher;

import java.io.File;
import java.io.IOException;

import net.sf.json.JSONObject;

import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;
import org.jvnet.hudson.plugins.purecoverage.parser.PureCoverageParser;
import org.kohsuke.stapler.StaplerRequest;

@SuppressWarnings("unchecked")
public class CoveragePublisher extends Publisher {

    private String coverageReportPattern;

    /**
     * @param coverageReportPattern - file name pattern that points to PureCoverage report 
     * @stapler-constructor
     */
    public CoveragePublisher(String coverageReportPattern) {
        this.coverageReportPattern = coverageReportPattern;
    }
    
    public void setCoverageReportPattern(String coverageReportPattern) {
    	this.coverageReportPattern = coverageReportPattern;
    }

    public String getCoverageReportPattern() {
        return coverageReportPattern;
    }

	@Override
    public Action getProjectAction(Project project) {
        return new CoverageProjectAction(project);
    }

    /**
     * {@inheritDoc}
     */
    public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
    	if (coverageReportPattern == null) {
    		listener.getLogger().println("Skipping coverage reports as coverageReportPattern is null");
    		return false;
    	}
        if (!Result.SUCCESS.equals(build.getResult())) {
        	listener.getLogger().println("Skipping coverage reports as the build was not successful...");
        	return true;
        }
        
        listener.getLogger().println("Publishing PureCoverage reports...");
        FilePath buildTarget = new FilePath(build.getRootDir());

        FilePath[] reports = new FilePath[0];
        final FilePath moduleRoot = build.getParent().getWorkspace();
        try {
            reports = moduleRoot.list(coverageReportPattern);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to find PureCoverage results"));
            build.setResult(Result.FAILURE);
        }

        if (reports.length == 0) {
            listener.getLogger().println("No coverage results were found using the pattern '" + coverageReportPattern
                    + "'.  Did you generate the report(s)?");
            build.setResult(Result.FAILURE);
            return true;
        }
        
        if (reports.length > 1) {
        	listener.getLogger().println("PureCoverage publisher found more than one report that match the pattern. "
        			+ "Currently, accumulating PureCoverage results is not implemented!");
        	build.setResult(Result.FAILURE);
        	return true;
        }

        FilePath singleReport = reports[0];
        final FilePath targetPath = new FilePath(buildTarget, CoverageReportsFinder.COVERAGE_PREFIX);
        try {
        	singleReport.copyTo(targetPath);
        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener.fatalError("Unable to copy coverage from " + singleReport + " to " + buildTarget));
            build.setResult(Result.FAILURE);
        }

        listener.getLogger().println("Parsing PureCoverage results...");
        ProjectCoverage projectCoverage = null;
        CoverageReportsFinder finder = new CoverageReportsFinder();
        for (File coverageResult : finder.findReports(build)) {
            try {
            	CoverageParser coverageParser = new PureCoverageParser();
				projectCoverage = coverageParser.parse(coverageResult);
            } catch (IOException e) {
                Util.displayIOException(e, listener);
                e.printStackTrace(listener.fatalError("Unable to parse " + coverageResult));
                build.setResult(Result.FAILURE);
                return false;
            }
        }
        
        CoverageResult coverageResult = new CoverageResult(build, projectCoverage);
        build.getActions().add(new CoverageBuildAction(build, coverageResult));

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Descriptor<Publisher> getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return DESCRIPTOR;
    }

    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    /**
     * Descriptor for {@link CoveragePublisher}. Used as a singleton. The class is marked as public so that it can be
     * accessed from views.
     * <p/>
     * <p/>
     * See <tt>views/hudson/plugins/coverage/CoveragePublisher/*.jelly</tt> for the actual HTML fragment for the
     * configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Publisher> {
        /**
         * Constructs a new DescriptorImpl.
         */
        DescriptorImpl() {
            super(CoveragePublisher.class);
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Publish PureCoverage Report";
        }

        /**
         * {@inheritDoc}
         */
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            return configure(req);
        }

        /**
         * {@inheritDoc}
         */
        public boolean configure(StaplerRequest req) throws FormException {
        	req.bindParameters(this, "pureCoverage.");
        	save();
        	return super.configure(req);
        }

        /**
         * Creates a new instance of {@link CoveragePublisher} from a submitted form.
         */
        public CoveragePublisher newInstance(StaplerRequest req) throws FormException {
        	CoveragePublisher instance = req.bindParameters(CoveragePublisher.class, "pureCoverage.");
            return instance;
        }
    }
}