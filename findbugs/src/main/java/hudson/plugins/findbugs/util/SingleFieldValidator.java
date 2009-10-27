package hudson.plugins.findbugs.util;

import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Form field validator base class with template method to validate a single field value.
 * You need the following steps to validate a single form field:
 * <ol>
 * <li>Create a subclass of {@link SingleFieldValidator}.</li>
 * <li>Create a subclass of {@link AbstractValidatorTest} with corresponding
 * unit tests.</li>
 * <li>Add a new entry in your jelly file with the attribute {@code field} named
 * like your publishers or reporters property, e.g.
 *
 * <pre>
 * &lt;f:editableComboBox id=&quot;defaultEncoding&quot; field=&quot;defaultEncoding&quot; items=&quot;${allEncodings}&quot;/&gt;
 * &lt;f:textbox field=&quot;threshold&quot;/&gt;
 * </pre>
 * <li>Create a method in your plug-in descriptor like {@link PluginDescriptor#doCheckDefaultEncoding(org.kohsuke.stapler.StaplerRequest, org.kohsuke.stapler.StaplerResponse)} for the property {@code defaultEncoding}.</li>
 * </ol>
 *
 * @author Ulli Hafner
 */
public abstract class SingleFieldValidator {
    private final String value;

    /**
     * Creates a new instance of <code>SingleFieldValidator</code>.
     * @param request
     *            Stapler request
     * @param response
     *            Stapler response
     */
    public SingleFieldValidator(final StaplerRequest request, final StaplerResponse response) {
        value = request.getParameter("value");
    }

    public final FormValidation check() throws IOException, ServletException {
        return check(value);
    }

    /**
     * Checks the input value.
     *
     * @param value
     *            the value to check
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ServletException
     *             the servlet exception
     */
    public abstract FormValidation check(final String value) throws IOException, ServletException;
}

