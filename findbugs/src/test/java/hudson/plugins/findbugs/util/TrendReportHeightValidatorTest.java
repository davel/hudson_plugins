package hudson.plugins.findbugs.util;

import org.junit.Test;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Tests the class {@link TrendReportHeightValidator}.
 *
 * @author Ulli Hafner
 */
public class TrendReportHeightValidatorTest extends AbstractValidatorTest {
    /**
     * Test some valid encodings.
     */
    @Test
    public void testValidEncodings() throws Exception {
        assertThatInputIsValid("50");
        assertThatInputIsValid("51");
        assertThatInputIsValid("52");
        assertThatInputIsValid("5000");
    }

    /**
     * Test some invalid encodings.
     */
    @Test
    public void testInvalidEncodings() throws Exception {
        assertThatInputIsInvalid("NIX");
        assertThatInputIsInvalid("-1");
        assertThatInputIsInvalid("49");
    }

    /** {@inheritDoc} */
    @Override
    protected SingleFieldValidator createValidator(final StaplerRequest request, final StaplerResponse response) {
        return new TrendReportHeightValidator(request, response);
    }
}
