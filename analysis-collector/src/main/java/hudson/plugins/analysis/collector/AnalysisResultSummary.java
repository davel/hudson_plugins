package hudson.plugins.analysis.collector;

/**
 * Creates the result summary for the collected analysis results. This summary will be
 * shown in the summary.jelly script of the {@link AnalysisResultAction}.
 *
 * @author Ulli Hafner
 */
public final class AnalysisResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createSummary(final AnalysisResult result) {
        StringBuilder summary = new StringBuilder();
        int bugs = result.getNumberOfAnnotations();

        summary.append(Messages.Analysis_ProjectAction_Name());
        summary.append(": ");
        if (bugs > 0) {
            summary.append("<a href=\"analysisResult\">");
        }
        if (bugs == 1) {
            summary.append(Messages.Analysis_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Analysis_ResultAction_MultipleWarnings(bugs));
        }
        if (bugs > 0) {
            summary.append("</a>");
        }
        summary.append(".");
        return summary.toString();
    }

    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createDeltaMessage(final AnalysisResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"analysisResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append(Messages.Analysis_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.Analysis_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"analysisResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.Analysis_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.Analysis_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }

    /**
     * Instantiates a new result summary.
     */
    private AnalysisResultSummary() {
        // prevents instantiation
    }
}

