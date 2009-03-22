package hudson.plugins.tasks.util;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Validates and converts threshold parameters. A threshold must be an integer
 * value greater or equal 0 or the empty string.
 *
 * @author Ulli Hafner
 */
public class ThresholdValidator extends SingleFieldValidator {
    /**
     * Creates a new instance of {@link ThresholdValidator}.
     *
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public ThresholdValidator(final StaplerRequest request, final StaplerResponse response) {
        super(request, response);
    }

    /** {@inheritDoc} */
    @Override
    public void check(final String value) throws IOException, ServletException {
        if (!StringUtils.isEmpty(value)) {
            try {
                int integer = Integer.valueOf(value);
                if (integer < 0) {
                    error(Messages.FieldValidator_Error_Threshold());
                    return;
                }
            }
            catch (NumberFormatException exception) {
                error(Messages.FieldValidator_Error_Threshold());
                return;
            }
        }

        ok();
    }


    /**
     * Returns whether the provided threshold string parameter is a valid
     * threshold number, i.e. an integer value greater or equal zero.
     *
     * @param threshold
     *            string representation of the threshold value
     * @return <code>true</code> if the provided threshold string parameter is a
     *         valid number >= 0
     */
    public static boolean isValid(final String threshold) {
        if (StringUtils.isNotBlank(threshold)) {
            try {
                return Integer.valueOf(threshold) >= 0;
            }
            catch (NumberFormatException exception) {
                // not valid
            }
        }
        return false;
    }



    /**
     * Converts the provided string threshold into an integer value.
     *
     * @param threshold
     *            string representation of the threshold value
     * @return integer threshold
     * @throws IllegalArgumentException
     *             if the provided string can't be converted to an integer value
     *             greater or equal zero
     */
    public static int convert(final String threshold) {
        if (isValid(threshold)) {
            if (StringUtils.isNotBlank(threshold)) {
                try {
                    return Integer.valueOf(threshold);
                }
                catch (NumberFormatException exception) {
                    // not valid
                }
            }
        }
        throw new IllegalArgumentException("Not a parsable integer value >= 0: " + threshold);
    }
}

