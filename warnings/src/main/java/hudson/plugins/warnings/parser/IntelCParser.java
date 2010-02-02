package hudson.plugins.warnings.parser;

import hudson.plugins.analysis.util.model.Priority;

import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;

/**
 * A parser for the Inter C compiler warnings.
 *
 * @author Vangelis Livadiotis
 */
public class IntelCParser extends RegexpLineParser {
    /** Warning type of this parser. */
    static final String WARNING_TYPE = "Intel";
    /** Pattern of Intel compiler warnings. */
    private static final String INTEL_PATTERN = "(.*)\\((\\d*)\\)?:.*((?:remark|warning|error)\\s*#*\\d*)\\s*:\\s*(.*)";

    /**
     * Creates a new instance of <code>InterCParser</code>.
     */
    public IntelCParser() {
        super(INTEL_PATTERN, "Intel compiler");
    }

    /** {@inheritDoc} */
    @Override
    protected Warning createWarning(final Matcher matcher) {
        String category = StringUtils.capitalize(matcher.group(3));

        Priority priority;
        if (StringUtils.startsWith(category, "Remark")) {
            priority = Priority.LOW;
        }
        else if (StringUtils.startsWith(category, "Error")) {
            priority = Priority.HIGH;
        }
        else {
            priority = Priority.NORMAL;
        }

        return new Warning(matcher.group(1), getLineNumber(matcher.group(2)), WARNING_TYPE,
                category, matcher.group(4), priority);
    }
}


