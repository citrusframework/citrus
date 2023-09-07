package org.citrusframework.reporter;

import java.util.Map;
import org.citrusframework.report.AbstractTestReporter;
import org.citrusframework.report.DefaultTestReporters;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporters;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestReportersFactoryTest {

    @Test
    public void testDefaultReporters() throws Exception {
        ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
        TestReportersFactory testReportersFactory = new TestReportersFactory();
        testReportersFactory.setApplicationContext(applicationContextMock);

        TestReporters testReporters = testReportersFactory.getObject();

        Assert.assertNotNull(testReporters);
        Assert.assertNotNull(testReporters.getTestReporters());

        DefaultTestReporters.DEFAULT_REPORTERS
            .forEach((reporterName, defaultReporter) ->
                Assert.assertListContainsObject(testReporters.getTestReporters(), defaultReporter, String.format("%s is missing in TestReporters", reporterName)));
    }

    @Test
    public void testDefaultReporterOverride() throws Exception {
        ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

        Map<String, AbstractTestReporter> specificReporters = Map.of("citrusLoggingReporter", new LoggingReporter(),
            "citrusHtmlReporter",new HtmlReporter(),
            "citrusJunitReporter",new JUnitReporter());
        Mockito.doReturn(specificReporters).when(applicationContextMock).getBeansOfType(TestReporter.class);
        Mockito.doReturn(true).when(applicationContextMock).containsBean("citrusLoggingReporter");
        Mockito.doReturn(true).when(applicationContextMock).containsBean("citrusHtmlReporter");
        Mockito.doReturn(true).when(applicationContextMock).containsBean("citrusJunitReporter");

        TestReportersFactory testReportersFactory = new TestReportersFactory();
        testReportersFactory.setApplicationContext(applicationContextMock);

        TestReporters testReporters = testReportersFactory.getObject();

        Assert.assertNotNull(testReporters);
        Assert.assertNotNull(testReporters.getTestReporters());

        DefaultTestReporters.DEFAULT_REPORTERS
            .forEach((reporterName, defaultReporter) ->
                Assert.assertListNotContainsObject(testReporters.getTestReporters(), defaultReporter, String.format("%s is missing in TestReporters", reporterName)));

        specificReporters
            .forEach((reporterName, specificReporter) ->
                Assert.assertListContainsObject(testReporters.getTestReporters(), specificReporter, String.format("%s is missing in TestReporters", reporterName)));

    }

}
