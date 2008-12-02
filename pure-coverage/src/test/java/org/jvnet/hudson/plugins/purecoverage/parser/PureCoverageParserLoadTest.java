package org.jvnet.hudson.plugins.purecoverage.parser;

import org.junit.Test;
import org.jvnet.hudson.plugins.purecoverage.domain.LineCoverageMetric;
import org.jvnet.hudson.plugins.purecoverage.domain.ProjectCoverage;
import org.jvnet.hudson.plugins.purecoverage.parser.PureCoverageParser;

import static junit.framework.Assert.*;

public class PureCoverageParserLoadTest extends ParserTestBase {

	@Test
	public void shouldParseLargeFile() throws Exception {
		PureCoverageParser parser = new PureCoverageParser();
		ProjectCoverage c = parser.parse(read("ShopTest.export"));
		
		//smoke tests
		assertTrue(c.getChildren().size() > 100);
		LineCoverageMetric firstChild = (LineCoverageMetric) c.getChildren().iterator().next();
		assertTrue(firstChild.getChildren().size() > 10);
	}
}