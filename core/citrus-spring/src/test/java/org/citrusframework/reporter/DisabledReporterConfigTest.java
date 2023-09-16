package org.citrusframework.reporter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration
@TestPropertySource(properties = {"citrus.default.logging.reporter.enabled=false",
        "citrus.default.html.reporter.enabled=false",
        "citrus.default.junit.reporter.enabled=false"})
public class DisabledReporterConfigTest extends UnitTestSupport {

    @Test
    public void testOverridesLoggingReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean("citrusLoggingReporter"));
        Assert.assertTrue(applicationContext.getBean("otherLoggingReporter") instanceof LoggingReporter);
    }

    @Test
    public void testOverridesJunitReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean("citrusJunitReporter"));
        Assert.assertTrue(applicationContext.getBean("otherJunitReporter") instanceof JUnitReporter);
    }

    @Test
    public void testOverridesHtmlReporter() {
        Assert.assertThrows(NoSuchBeanDefinitionException.class, () -> applicationContext.getBean("citrusHtmlReporter"));
        Assert.assertTrue(applicationContext.getBean("otherHtmlReporter") instanceof HtmlReporter);
    }

    @Configuration
    public static class OtherReporterConfiguration {

        @Bean
        public LoggingReporter otherLoggingReporter() {
            return new LoggingReporter();
        }

        @Bean
        public JUnitReporter otherJunitReporter() {
            return new JUnitReporter();
        }

        @Bean
        public HtmlReporter otherHtmlReporter() {
            return new HtmlReporter();
        }
    }

}
