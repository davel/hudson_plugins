package hudson.plugins.coverage.impl;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.junit.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.hamcrest.CoreMatchers.*;

import hudson.plugins.coverage.model.Instance;

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
        assumeThat(true, is(true));
        // TODO write some real tests
        assertThat("write this test", containsString("test written"));
    }
}
