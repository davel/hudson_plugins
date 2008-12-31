package hudson.plugins.pmd;


/**
 * Represents the result summary of the PMD parser. This summary will be
 * shown in the summary.jelly script of the PMD result action.
 *
 * @author Ulli Hafner
 */
public final class ResultSummary {
    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createSummary(final PmdResult result) {
        StringBuilder summary = new StringBuilder();
        int bugs = result.getNumberOfAnnotations();

        summary.append("PMD: ");
        if (bugs > 0) {
            summary.append("<a href=\"pmdResult\">");
        }
        if (bugs == 1) {
            summary.append(Messages.PMD_ResultAction_OneWarning());
        }
        else {
            summary.append(Messages.PMD_ResultAction_MultipleWarnings(bugs));
        }
        if (bugs > 0) {
            summary.append("</a>");
        }
        summary.append(" ");
        if (result.getNumberOfModules() == 1) {
            summary.append(Messages.PMD_ResultAction_OneFile());
        }
        else {
            summary.append(Messages.PMD_ResultAction_MultipleFiles(result.getNumberOfModules()));
        }
        return summary.toString();
    }

    /**
     * Returns the message to show as the result summary.
     *
     * @param result
     *            the result
     * @return the message
     */
    public static String createDeltaMessage(final PmdResult result) {
        StringBuilder summary = new StringBuilder();
        if (result.getNumberOfNewWarnings() > 0) {
            summary.append("<li><a href=\"pmdResult/new\">");
            if (result.getNumberOfNewWarnings() == 1) {
                summary.append(Messages.PMD_ResultAction_OneNewWarning());
            }
            else {
                summary.append(Messages.PMD_ResultAction_MultipleNewWarnings(result.getNumberOfNewWarnings()));
            }
            summary.append("</a></li>");
        }
        if (result.getNumberOfFixedWarnings() > 0) {
            summary.append("<li><a href=\"pmdResult/fixed\">");
            if (result.getNumberOfFixedWarnings() == 1) {
                summary.append(Messages.PMD_ResultAction_OneFixedWarning());
            }
            else {
                summary.append(Messages.PMD_ResultAction_MultipleFixedWarnings(result.getNumberOfFixedWarnings()));
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

