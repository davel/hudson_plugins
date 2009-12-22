package hudson.plugins.codescanner;

import static org.easymock.classextension.EasyMock.*;
import hudson.plugins.analysis.test.AbstractEnglishLocaleTest;
import junit.framework.Assert;

import org.junit.Test;

/**
 * Tests the class {@link ResultSummary}.
 */
public class ResultSummaryTest extends AbstractEnglishLocaleTest {
    /**
     * Checks the text for no warning.
     */
    @Test
    public void test0Warnings() {
        checkSummaryText(0, "Symbian CodeScanner results: 0 warnings.");
    }

    /**
     * Checks the text for 1 warning.
     */
    @Test
    public void test1Warning() {
        checkSummaryText(1, "Symbian CodeScanner results: <a href=\"codescannerResult\">1 warning</a>.");
    }

    /**
     * Checks the text for 5 Codescanner.
     */
    @Test
    public void test5WarningsIn1File() {
        checkSummaryText(5, "Symbian CodeScanner results: <a href=\"codescannerResult\">5 warnings</a>.");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfWarnings
     *            the number of warnings
     * @param expectedMessage
     *            the expected message
     */
    private void checkSummaryText(final int numberOfWarnings, final String expectedMessage) {
        CodescannerResult result = createMock(CodescannerResult.class);
        expect(result.getNumberOfAnnotations()).andReturn(numberOfWarnings).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong summary message created.", expectedMessage, ResultSummary.createSummary(result));

        verify(result);
    }

    /**
     * Checks the delta message for no new and no fixed Codescanner.
     */
    @Test
    public void testNoDelta() {
        checkDeltaText(0, 0, "");
    }

    /**
     * Checks the delta message for 1 new and no fixed Codescanner.
     */
    @Test
    public void testOnly1New() {
        checkDeltaText(0, 1, "<li><a href=\"codescannerResult/new\">1 new warning</a></li>");
    }

    /**
     * Checks the delta message for 5 new and no fixed Codescanner.
     */
    @Test
    public void testOnly5New() {
        checkDeltaText(0, 5, "<li><a href=\"codescannerResult/new\">5 new warnings</a></li>");
    }

    /**
     * Checks the delta message for 1 fixed and no new Codescanner.
     */
    @Test
    public void testOnly1Fixed() {
        checkDeltaText(1, 0, "<li><a href=\"codescannerResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and no new Codescanner.
     */
    @Test
    public void testOnly5Fixed() {
        checkDeltaText(5, 0, "<li><a href=\"codescannerResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new Codescanner.
     */
    @Test
    public void test5New5Fixed() {
        checkDeltaText(5, 5,
                "<li><a href=\"codescannerResult/new\">5 new warnings</a></li>"
                + "<li><a href=\"codescannerResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new Codescanner.
     */
    @Test
    public void test5New1Fixed() {
        checkDeltaText(1, 5,
        "<li><a href=\"codescannerResult/new\">5 new warnings</a></li>"
        + "<li><a href=\"codescannerResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new Codescanner.
     */
    @Test
    public void test1New5Fixed() {
        checkDeltaText(5, 1,
                "<li><a href=\"codescannerResult/new\">1 new warning</a></li>"
                + "<li><a href=\"codescannerResult/fixed\">5 fixed warnings</a></li>");
    }

    /**
     * Checks the delta message for 5 fixed and 5 new Codescanner.
     */
    @Test
    public void test1New1Fixed() {
        checkDeltaText(1, 1,
                "<li><a href=\"codescannerResult/new\">1 new warning</a></li>"
                + "<li><a href=\"codescannerResult/fixed\">1 fixed warning</a></li>");
    }

    /**
     * Parameterized test case to check the message text for the specified
     * number of warnings and files.
     *
     * @param numberOfFixedWarnings
     *            the number of fixed warnings
     * @param numberOfNewWarnings
     *            the number of new warnings
     * @param expectedMessage
     *            the expected message
     */
    private void checkDeltaText(final int numberOfFixedWarnings, final int numberOfNewWarnings, final String expectedMessage) {
        CodescannerResult result = createMock(CodescannerResult.class);
        expect(result.getNumberOfFixedWarnings()).andReturn(numberOfFixedWarnings).anyTimes();
        expect(result.getNumberOfNewWarnings()).andReturn(numberOfNewWarnings).anyTimes();

        replay(result);

        Assert.assertEquals("Wrong delta message created.", expectedMessage, ResultSummary.createDeltaMessage(result));

        verify(result);
    }
}

