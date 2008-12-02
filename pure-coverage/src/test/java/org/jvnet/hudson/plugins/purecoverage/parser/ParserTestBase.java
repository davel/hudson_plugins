package org.jvnet.hudson.plugins.purecoverage.parser;

import static junit.framework.Assert.assertNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ParserTestBase {
	
	protected Reader read(String reportFile) {
		String pkg = getClass().getPackage().getName().replaceAll("\\.", "/");
		InputStream resource = getClass().getClassLoader().getResourceAsStream(pkg + "/" + reportFile);
		assertNotNull("could not find resource", resource);
		
		return new InputStreamReader(resource);
	}

}
