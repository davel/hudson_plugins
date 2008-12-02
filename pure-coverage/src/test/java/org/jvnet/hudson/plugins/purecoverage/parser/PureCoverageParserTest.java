package org.jvnet.hudson.plugins.purecoverage.parser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;
import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverageMetric;
import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;
import org.jvnet.hudson.plugins.purecoverage.parser.PureCoverageParser;
import org.jvnet.hudson.plugins.purecoverage.util.UrlTransformer;

public class PureCoverageParserTest extends ParserTestBase {

	@Test
	public void shouldNotHaveNANs() {
		PureCoverageParser parser = new PureCoverageParser();
		ProjectCoverage c = parser.parse(read("simple.export"));
		LineCoverageMetric dir = c.getChild(uniqueUrl("noCoverageDirectory"));
		assertTotalLines(dir, 0);
		assertCoveredLines(dir, 0);
		assertEquals("0.0% (0/0)", dir.getLineCoverage().toString());
	}
	
	@Test
	public void shouldParseSimpleReport() {
		PureCoverageParser parser = new PureCoverageParser();
		ProjectCoverage c = parser.parse(read("simple.export"));
		
		assertTotalLines(c, 154530);
		assertCoveredLines(c, 154530 - 131726);
		
		//first dir
		LineCoverageMetric dir = c.getChild(uniqueUrl("/vobs/ehtl/IDL/src/"));
		assertTotalLines(dir, 41776);
		assertCoveredLines(dir, 41776 - 41687);

		//last dir
		dir = c.getChild(uniqueUrl("/vobs/Meridian/IDL/source/"));
		
		//first file
		LineCoverageMetric file = dir.getChild(uniqueUrl("OSMloadBalancerStub.cpp"));
		assertTotalLines(file, 4586);
		assertCoveredLines(file, 4586 - 4314);
		
		//last file
		file = dir.getChild(uniqueUrl("eHotels_c.hh"));
		assertTotalLines(file, 3146);
		assertCoveredLines(file, 3146 -	3146);
		
		//first function
		LineCoverageMetric function = file.getChild(uniqueUrl("eHotels::var&eHotels<Foo>::operator=(Type*)"));
		assertTotalLines(function, 4);
		assertCoveredLines(function, 4 - 2);
		
		//last function
		assertNotNull(file.getChild(uniqueUrl("fooFunction")));
	}

	private String uniqueUrl(String name) {
		return new UrlTransformer().toUniqueUrl(name);
	}

	private void assertTotalLines(LineCoverageMetric c, int totalLines) {
		assertEquals(totalLines, c.getLineCoverage().getTotalLines());
	}
	
	private void assertCoveredLines(LineCoverageMetric c, int coveredLines) {
		assertEquals(coveredLines, c.getLineCoverage().getCoveredLines());
	}
}