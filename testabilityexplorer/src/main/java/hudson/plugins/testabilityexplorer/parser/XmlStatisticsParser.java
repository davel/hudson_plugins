package hudson.plugins.testabilityexplorer.parser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.*;

import hudson.plugins.testabilityexplorer.report.costs.Statistic;
import hudson.plugins.testabilityexplorer.report.costs.CostSummary;
import hudson.plugins.testabilityexplorer.parser.selectors.ConverterSelector;
import hudson.plugins.testabilityexplorer.parser.converters.ElementConverter;

/**
 * Parses and merges Testability Explorer XML reports.
 *
 * @author reik.schatz
 */
public class XmlStatisticsParser extends StatisticsParser
{
    private ConverterSelector m_converterSelector = null;

    public XmlStatisticsParser(ConverterSelector converterSelector)
    {
        m_converterSelector = converterSelector;
    }

    public Collection<Statistic> parse(File inFile)
    {
        Collection<Statistic> statistics = new ArrayList<Statistic>();
        FileInputStream fileInputStream = null;
        try
        {
            fileInputStream = new FileInputStream(inFile);
            statistics = parse(fileInputStream);
        }
        catch (FileNotFoundException e)
        {
            String filePath = inFile != null ? inFile.getPath() : "";
            throw new IllegalStateException("Unable to find input file (" + filePath + ").", e);
        }
        finally
        {
            IOUtils.closeQuietly(fileInputStream);
        }
        return statistics;
    }

    public Collection<Statistic> parse(InputStream inputStream)
    {
        Collection<Statistic> results = new ArrayList<Statistic>();
        BufferedInputStream bufferedInputStream = null;

        try
        {
            bufferedInputStream = new BufferedInputStream(inputStream);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(bufferedInputStream, null);

            CostSummary overallCost = processRootElement(xpp);
            results.add(new Statistic(overallCost));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to process report file.", e);
        }
        catch (XmlPullParserException e)
        {
            throw new IllegalStateException("Unable to parse report file.", e);
        }
        finally
        {
            IOUtils.closeQuietly(bufferedInputStream);
        }
        sortByCost(results);
        return results;
    }

    private void sortByCost(Collection<Statistic> results)
    {
        for (Statistic result : results)
        {
            result.sort();
        }
    }

    /**
     * Creates a new {@link CostSummary} from the specified parser. The parser is expected to be at the root element.
     *
     * @param xpp an {@link XmlPullParser}
     * @return CostSummary
     * @throws IOException if the underlaying file or stream cannot be processed
     * @throws XmlPullParserException if the underlaying xml cannot be parsed
     */
    protected CostSummary processRootElement(XmlPullParser xpp) throws IOException, XmlPullParserException
    {
        CostSummary overallCost = null;
        int eventType = xpp.getEventType();
        do {
            // we are only interested in xml elements
            if(eventType == XmlPullParser.START_TAG) {

                String elementName = xpp.getName();

                ElementConverter converter = m_converterSelector.getConverter(elementName);
                if (converter == null)
                {
                    throw new IllegalStateException("The " + m_converterSelector.getClass().getSimpleName() + " was unable to return a ElementConverter for XML tag name: " + elementName);
                }

                if (overallCost == null)
                {
                    overallCost = (CostSummary) converter.construct(xpp, null);
                }
                else
                {
                    converter.construct(xpp, overallCost);
                }
            }
            eventType = xpp.next();
        } while (eventType != XmlPullParser.END_DOCUMENT);
        return overallCost;
    }
}
