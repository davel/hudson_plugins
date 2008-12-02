package org.jvnet.hudson.plugins.purecoverage.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.jvnet.hudson.plugins.purecoverage.util.UrlTransformer;

public class UrlTransformerTest {

	@Test
	public void shouldMakeUrl() {
		UrlTransformer transformer = new UrlTransformer();
		String u = transformer.toUniqueUrl("12345asdf_!@#$%^&*()_");
		assertEquals("12345asdf_____________-1594828599", u);
	}
}