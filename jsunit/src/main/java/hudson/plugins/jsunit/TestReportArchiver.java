package hudson.plugins.jsunit;

/**
 * Interface for mocking out the JUnitArchiver from tests.
 */
public interface TestReportArchiver {
    /**
     * Performs the archiving of tests
     * @return true, if it was successful; false otherwise
     */
    boolean archive() throws java.lang.InterruptedException,
            java.io.IOException;
}
