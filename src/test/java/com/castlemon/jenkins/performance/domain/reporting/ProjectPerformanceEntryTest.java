package com.castlemon.jenkins.performance.domain.reporting;

import org.agileware.test.PropertiesTester;
import org.junit.Test;

public class ProjectPerformanceEntryTest {

	@Test
	public void test() throws Exception {
		PropertiesTester tester = new PropertiesTester();
		tester.testAll(ProjectPerformanceEntry.class);
	}

}