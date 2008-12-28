package hudson.plugins.testabilityexplorer.parser.selectors;

import hudson.plugins.testabilityexplorer.parser.converters.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Returns {@link ElementConverter}'s based on the given XML element tag names.
 *
 * @author reik.schatz
 */
public class DefaultConverterSelector implements ConverterSelector
{
    static final String TAG_TESTABILITY = "testability";
    static final String TAG_CLASS = "class";
    static final String TAG_METHOD = "method";
    static final String TAG_COST = "cost";

    private List<NamedConverter> m_converters;

    /**
     * Initializes the {@link DefaultConverterSelector} with four {@link ElementConverter}s. The
     * converters to be added are of type
     * <ul>
     *  <li>{@code TestabilityElementConverter}
     *  <li>{@code ClassElementConverter}
     *  <li>{@code MethodElementConverter}
     *  <li>{@code CostElementConverter}
     * </ul>
     * and will be returned if you call {@link DefaultConverterSelector#getConverter(String)} with
     * one of these String's
     * <ul>
     *  <li>{@code testability}
     *  <li>{@code class}
     *  <li>{@code method}
     *  <li>{@code cost}
     * </ul>
     */
    public DefaultConverterSelector()
    {
        initializeConverters(
            new NamedConverter(TAG_TESTABILITY, new TestabilityElementConverter()),
            new NamedConverter(TAG_CLASS, new ClassElementConverter()),
            new NamedConverter(TAG_METHOD, new MethodElementConverter()),
            new NamedConverter(TAG_COST, new CostElementConverter())
        );
    }

    private void initializeConverters(NamedConverter... converters)
    {
        m_converters = new ArrayList<NamedConverter>();
        m_converters.addAll(Arrays.asList(converters));
    }

    /**
     * Returns a {@link ElementConverter} that is mapped to the
     * given {@code elementName}. This {@link ConverterSelector} will
     * return a {@link ElementConverter} for the following {@code elementName}'s:
     * <ul>
     *  <li>{@code testability}
     *  <li>{@code class}
     *  <li>{@code method}
     *  <li>{@code cost}
     * </ul>
     *
     * If called with {@code null} or any other String, this method returns {@code nuĺl}.
     *
     * @param elementName String
     * @return {@link ElementConverter} or {@code null}
     */
    public ElementConverter getConverter(String elementName)
    {
        ElementConverter converter = null;
        if (elementName != null && elementName.length() > 0)
        {
            for (NamedConverter namedConverter : m_converters)
            {
                String name = namedConverter.getName();
                if (elementName.equals(name))
                {
                    converter = namedConverter.getConverter();
                    break;
                }
            }
        }
        return converter;
    }
}
