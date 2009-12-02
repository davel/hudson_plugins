package hudson.plugins.cppncss.parser;

import hudson.plugins.cppncss.parser.Statistic;
import junit.framework.TestCase;

import java.net.URL;
import java.io.File;
import java.util.Collection;

/**
 * TODO javadoc.
 *
 * @author Stephen Connolly
 * @since 25-Feb-2008 22:37:25
 */
public class StatisticTest extends TestCase {

    public StatisticTest(String name) {
        super(name);
    }

    public void testAntSmoke() throws Exception {
        File inputFile = new File(getClass().getResource("ant-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r = Statistic.parse(inputFile);

        Statistic expected = new Statistic("");
        expected.setCcn(20);
        expected.setFunctions(9);
        expected.setNcss(129);

        assertEquals(expected, Statistic.total(r.getFileResults()));
        
        expected = new Statistic("");
        expected.setCcn(20);
        expected.setNcss(59);
        assertEquals(expected, Statistic.total(r.getFunctionResults()));
    }

    public void testMaven2Smoke() throws Exception {
        File inputFile = new File(getClass().getResource("m2-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r = Statistic.parse(inputFile);

        Statistic expected = new Statistic("");
        expected.setCcn(20);
        expected.setFunctions(9);
        expected.setNcss(129);

        assertEquals(expected, Statistic.total(r.getFileResults()));
    }

    public void testMerge() throws Exception {
        File inputFile = new File(getClass().getResource("ant-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r1 = Statistic.parse(inputFile);

        inputFile = new File(getClass().getResource("m2-cppncss-report.xml").getFile()).getAbsoluteFile();

        StatisticsResult r2 = Statistic.parse(inputFile);

        Statistic expected = new Statistic("");
        expected.setCcn(40);
        expected.setFunctions(18);
        expected.setNcss(258);

        assertEquals(expected, Statistic.total(Statistic.merge(r1.getFileResults(), r2.getFileResults())));

    }
}
