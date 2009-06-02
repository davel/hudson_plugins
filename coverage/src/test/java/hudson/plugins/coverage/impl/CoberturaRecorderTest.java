package hudson.plugins.coverage.impl;

import hudson.plugins.coverage.model.Instance;
import hudson.plugins.coverage.model.Metric;
import hudson.plugins.coverage.model.measurements.LineCoverage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLEventReader2;
import org.codehaus.stax2.LocationInfo;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 *
 * @author connollys
 * @since Nov 18, 2008 3:22:29 PM
 */
public class CoberturaRecorderTest {

    @Test
    public void smokeTest() throws URISyntaxException {
        File resultsFile = new File(getClass().getResource("cobertura-coverage.xml").toURI());
        CoberturaRecorder instance =
                new CoberturaRecorder(Collections.singleton(resultsFile), resultsFile.getParentFile());
        Instance results = Instance.newInstance(Collections.singleton(instance));
        System.out.println(results);
        assertThat(results.getMeasurement(Metric.LINE_COVERAGE), is(LineCoverage.class));
        assertThat("Total count of lines",
                LineCoverage.class.cast(results.getMeasurement(Metric.LINE_COVERAGE)).getCount(),
                is(59));
        assertThat("Covered count of lines",
                LineCoverage.class.cast(results.getMeasurement(Metric.LINE_COVERAGE)).getCover(),
                is(0));
    }
}
