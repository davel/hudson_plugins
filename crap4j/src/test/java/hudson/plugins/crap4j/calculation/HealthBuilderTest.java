package hudson.plugins.crap4j.calculation;

import junit.framework.TestCase;

public class HealthBuilderTest extends TestCase {
	
	public HealthBuilderTest() {
		super();
	}
	
	public void testIllegalThresholdsZero() {
		try {
			new HealthBuilder(0.0d);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The threshold needs to be positive, and not 0.0", e.getMessage());
		}
	}

	public void testIllegalThresholdsNegative() {
		try {
			new HealthBuilder(-1.0d);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("The threshold needs to be positive, and not -1.0", e.getMessage());
		}
	}

	public void testInstantiation() {
		HealthBuilder builder = new HealthBuilder(10.0d);
		assertEquals(10.0d, builder.getThreshold(), 1E-9);
	}
	
	public void testDefaultThreshold() {
		HealthBuilder builder = new HealthBuilder();
		assertEquals(15.0d, builder.getThreshold(), 1E-9);
	}

	public void testHealthCalculation() throws Exception {
		HealthBuilder builder = new HealthBuilder();
		assertEquals(0.0d, builder.calculateHealthOf(25.0d), 1E-6);
		assertEquals(0.0d, builder.calculateHealthOf(15.0d), 1E-6);
		assertEquals(100.0d, builder.calculateHealthOf(0.0d), 1E-6);
		assertEquals(50.0d, builder.calculateHealthOf(7.5d), 1E-6);
		assertEquals(10.0d, builder.calculateHealthOf(13.5d), 1E-6);
		assertEquals(90.0d, builder.calculateHealthOf(1.5d), 1E-6);
	}

	public void testHealthCalculationWithCustomThreshold() throws Exception {
		HealthBuilder builder = new HealthBuilder(10.0d);
		assertEquals(0.0d, builder.calculateHealthOf(25.0d), 1E-6);
		assertEquals(0.0d, builder.calculateHealthOf(15.0d), 1E-6);
		assertEquals(100.0d, builder.calculateHealthOf(0.0d), 1E-6);
		assertEquals(25.0d, builder.calculateHealthOf(7.5d), 1E-6);
		assertEquals(0.0d, builder.calculateHealthOf(13.5d), 1E-6);
		assertEquals(85.0d, builder.calculateHealthOf(1.5d), 1E-6);
	}

	public void testHealthSummary() throws Exception {
		HealthBuilder builder = new HealthBuilder();
		assertEquals("132 crappy methods (7.96%)", Messages.HealthBuilder_HealthSummary(132, 7.96d));
		assertEquals("0 crappy methods (0%)", Messages.HealthBuilder_HealthSummary(0, 0.0d));
		assertEquals("1 crappy methods (0.02%)", Messages.HealthBuilder_HealthSummary(1, 0.02d));
	}
}
