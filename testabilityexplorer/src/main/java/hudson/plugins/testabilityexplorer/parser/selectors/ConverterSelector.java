package hudson.plugins.testabilityexplorer.parser.selectors;

import hudson.plugins.testabilityexplorer.parser.converters.ElementConverter;


/**
 * Returns {@link ElementConverter}'s based on Strings.
 *
 * @author reik.schatz
 */
public interface ConverterSelector
{
    /**
     * Returns an {@link ElementConverter} mapped to the specified {@code elementName}.
     * @param elementName String
     * @return ElementConverter
     */
    public ElementConverter getConverter(String elementName);
}
