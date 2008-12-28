package hudson.plugins.testabilityexplorer.parser.converters;

/**
 * Wraps a {@link ElementConverter} so it can have a name.
 *
 * @author reik.schatz
 */
public class NamedConverter
{
    private String m_name;
    private ElementConverter m_converter;

    public NamedConverter(String name, ElementConverter converter)
    {
        m_name = name;
        m_converter = converter;
    }

    public String getName()
    {
        return m_name;
    }

    public ElementConverter getConverter()
    {
        return m_converter;
    }
}
