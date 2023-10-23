package org.citrusframework.reporter;

import static org.citrusframework.reporter.ReporterConfig.CITRUS_HTML_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_JUNIT_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_LOGGING_REPORTER;

import org.citrusframework.UnitTestSupport;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.Test;


@TestPropertySource(value = "classpath:org/citrusframework/reporter/default-logger-disabled.properties")
public class ReporterConfigDisabledTest extends UnitTestSupport {

    @Test
    public void testDefaultLoggingReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(CITRUS_LOGGING_REPORTER));
    }

    @Test
    public void testDefaultJunitReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(CITRUS_JUNIT_REPORTER));
    }

    @Test
    public void testDefaultHtmlReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean(CITRUS_HTML_REPORTER));
    }

}
