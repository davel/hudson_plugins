package hudson.plugins.tasks.util;

import hudson.util.FormFieldValidator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Validates a file encoding. The encoding must be an encoding ID supported by
 * the underlying Java platform.
 *
 * @author Ulli Hafner
 */
public class EncodingValidator extends FormFieldValidator implements Validator {
    /** All available character sets. */
    private static final Set<String> ALL_CHARSETS = Collections.unmodifiableSet(new HashSet<String>(Charset.availableCharsets().keySet()));
    /** Error message. */
    private static final String MESSAGE = "Encoding must be a supported encoding of the Java platform (see java.nio.charset.Charset).";

    /**
     * Creates a new instance of <code>NumberValidator</code>.
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public EncodingValidator(final StaplerRequest request, final StaplerResponse response) {
        super(request, response, false);
    }

    /**
     * Returns all available character set names.
     *
     * @return all available character set names
     */
    public static Set<String> getAvailableCharsets() {
        return ALL_CHARSETS;
    }

    /** {@inheritDoc} */
    @Override
    public void check() throws IOException, ServletException {
        String encoding = request.getParameter("value");
        try {
            if (StringUtils.isEmpty(encoding) || Charset.forName(encoding) != null) {
                ok();
            }
        }
        catch (IllegalCharsetNameException exception) {
            error(MESSAGE);
        }
        catch (UnsupportedCharsetException exception) {
            error(MESSAGE);
        }
    }
}

