package hudson.plugins.codescanner;

/**
 * Represents the result summary of the warnings parser. This summary will be
 * shown in the summary.jelly script of the warnings result action.
 *
 * @author Maximilian Odendahl
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createSummary(final CodescannerResult result) {
        StringBuilder summary = new StringBuilder();
        int bugs = result.getNumberOfAnnotations();

        summary.append(Messages.Codescanner_ProjectAction_Name());
        summary.append(": ");
        if (bugs > 0) {
            summary.append("<a href=\"codescannerResult\">");
        }
        if (bugs == 1) {
            summary.append(Messages.Codescanner_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.Codescanner_ResultAction_MultipleWarnings(bugs));
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
    public static String createDeltaMessage(final CodescannerResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"codescannerResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append(Messages.Codescanner_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.Codescanner_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"codescannerResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.Codescanner_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.Codescanner_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
            }
            summary.append("</a></li>");
        }

        return summary.toString();
    }

    /**
     * Instantiates a new result summary.
     */
    private ResultSummary() {
        // prevents instantiation
    }
}

