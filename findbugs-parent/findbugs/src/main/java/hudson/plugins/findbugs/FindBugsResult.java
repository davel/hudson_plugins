package hudson.plugins.findbugs;

import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.ParserResult;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.findbugs.parser.Bug;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the results of the FindBugs analysis. One instance of this class is persisted for
 * each build via an XML file.
 *
 * @author Ulli Hafner
 */
public class FindBugsResult extends BuildResult {
    /** Unique identifier of this class. */
    private static final long serialVersionUID = 2768250056765266658L;

    /**
     * Creates a new instance of {@link FindBugsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     */
    public FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result) {
        super(build, defaultEncoding, result);
    }

    /**
     * Creates a new instance of {@link FindBugsResult}.
     *
     * @param build
     *            the current build as owner of this action
     * @param defaultEncoding
     *            the default encoding to be used when reading and parsing files
     * @param result
     *            the parsed result with all annotations
     * @param previous
     *            the result of the previous build
     */
    public FindBugsResult(final AbstractBuild<?, ?> build, final String defaultEncoding,
            final ParserResult result, final FindBugsResult previous) {
        super(build, defaultEncoding, result, previous);
    }

    /** {@inheritDoc} */
    @Override
    protected void configure(final XStream xstream) {
        xstream.alias("bug", Bug.class);
    }

    /**
     * Returns a summary message for the summary.jelly file.
     *
     * @return the summary message
     */
    public String getSummary() {
        return ResultSummary.createSummary(this);
    }

    /** {@inheritDoc} */
    @Override
    public String getDetails() {
        String message = ResultSummary.createDeltaMessage(this);
        if (getNumberOfAnnotations() == 0 && getDelta() == 0) {
            message += "<li>" + Messages.FindBugs_ResultAction_NoWarningsSince(getZeroWarningsSinceBuild()) + "</li>";
            message += createHighScoreMessage();
        }
        return message;
    }


    /**
     * Creates a highscore message.
     *
     * @return a highscore message
     */
    private String createHighScoreMessage() {
        if (isNewZeroWarningsHighScore()) {
            long days = getDays(getZeroWarningsHighScore());
            if (days == 1) {
                return "<li>" + Messages.FindBugs_ResultAction_OneHighScore() + "</li>";
            }
            else {
                return "<li>" + Messages.FindBugs_ResultAction_MultipleHighScore(days) + "</li>";
            }
        }
        else {
            long days = getDays(getHighScoreGap());
            if (days == 1) {
                return "<li>" + Messages.FindBugs_ResultAction_OneNoHighScore() + "</li>";
            }
            else {
                return "<li>" + Messages.FindBugs_ResultAction_MultipleNoHighScore(days) + "</li>";
            }
        }
    }

    /**
     * Returns the name of the file to store the serialized annotations.
     *
     * @return the name of the file to store the serialized annotations
     */
    @Override
    protected String getSerializationFileName() {
        return "findbugs-warnings.xml";
    }

    /** {@inheritDoc} */
    public String getDisplayName() {
        return Messages.FindBugs_ProjectAction_Name();
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ResultAction<? extends BuildResult>> getResultActionType() {
        return FindBugsResultAction.class;
    }
}
