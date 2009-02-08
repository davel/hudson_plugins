package hudson.plugins.crap4j.reader.util;

import java.util.Locale;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.schneide.crap4j.reader.util.NumericalParser;

public class NumericalParserTest extends TestCase {
	
	public NumericalParserTest() {
		super();
	}
	
	public void testGermanDoubleRepresentation() {
		NumericalParser parser = new NumericalParser(Locale.GERMAN);
		Assert.assertEquals(1.00d, parser.parseDouble("1,00"), 1E-9);
		Assert.assertEquals(60036.00d, parser.parseDouble("60036,00"), 1E-9);
		Assert.assertEquals(7.16d, parser.parseDouble("7,16"), 1E-9);
	}

	public void testEnglishDoubleRepresentation() {
		NumericalParser parser = new NumericalParser(Locale.ENGLISH);
		Assert.assertEquals(1.00d, parser.parseDouble("1.00"), 1E-9);
		Assert.assertEquals(60036.00d, parser.parseDouble("60036.00"), 1E-9);
		Assert.assertEquals(7.16d, parser.parseDouble("7.16"), 1E-9);
	}

}
