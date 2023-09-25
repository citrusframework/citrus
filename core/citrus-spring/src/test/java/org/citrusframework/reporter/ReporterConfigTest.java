package org.citrusframework.reporter;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.reporter.ReporterConfig.HtmlReporterEnablementCondition;
import org.citrusframework.reporter.ReporterConfig.JunitReporterEnablementCondition;
import org.citrusframework.reporter.ReporterConfig.LoggingReporterEnablementCondition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.reporter.ReporterConfig.CITRUS_HTML_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_JUNIT_REPORTER;
import static org.citrusframework.reporter.ReporterConfig.CITRUS_LOGGING_REPORTER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ReporterConfigTest extends UnitTestSupport {

    @Test
    public void testDefaultLoggingReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_LOGGING_REPORTER) instanceof  LoggingReporter);
    }

    @Test
    public void testDefaultJunitReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_JUNIT_REPORTER) instanceof  JUnitReporter);
    }

    @Test
    public void testDefaultHtmlReporter() {
        Assert.assertTrue(applicationContext.getBean(CITRUS_HTML_REPORTER) instanceof  HtmlReporter);
    }
    @Test
    public void testLoggerReporterConditionDisabled() {

        Environment environmentMock = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactoryMock = mock(ConfigurableListableBeanFactory.class);
        ConditionContext conditionContextMock = mock(ConditionContext.class);
        doReturn(environmentMock).when(conditionContextMock).getEnvironment();
        doReturn(beanFactoryMock).when(conditionContextMock).getBeanFactory();
        doReturn(new LoggingReporter()).when(beanFactoryMock).getBean(CITRUS_LOGGING_REPORTER);

        LoggingReporterEnablementCondition condition = new LoggingReporterEnablementCondition();
        AnnotatedTypeMetadata annotatedTypeMetadataMock = mock(AnnotatedTypeMetadata.class);
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

        doReturn(null).when(beanFactoryMock).getBean(CITRUS_LOGGING_REPORTER);
        doReturn("false").when(environmentMock).getProperty(ReporterConfig.DEFAULT_LOGGING_REPORTER_ENABLED_PROPERTY, "true");
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

    }

    @Test
    public void testJunitReporterConditionDisabledByOtherBean() {

        Environment environmentMock = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactoryMock = mock(ConfigurableListableBeanFactory.class);
        ConditionContext conditionContextMock = mock(ConditionContext.class);
        doReturn(environmentMock).when(conditionContextMock).getEnvironment();
        doReturn(beanFactoryMock).when(conditionContextMock).getBeanFactory();
        doReturn(new LoggingReporter()).when(beanFactoryMock).getBean(CITRUS_JUNIT_REPORTER);

        JunitReporterEnablementCondition condition = new JunitReporterEnablementCondition();
        AnnotatedTypeMetadata annotatedTypeMetadataMock = mock(AnnotatedTypeMetadata.class);
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

        doReturn(null).when(beanFactoryMock).getBean(CITRUS_JUNIT_REPORTER);
        doReturn("false").when(environmentMock).getProperty(ReporterConfig.DEFAULT_JUNIT_REPORTER_ENABLED_PROPERTY, "true");
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

    }

    @Test
    public void testHtmlReporterConditionDisabledByOtherBean() {

        Environment environmentMock = mock(Environment.class);
        ConfigurableListableBeanFactory beanFactoryMock = mock(ConfigurableListableBeanFactory.class);
        ConditionContext conditionContextMock = mock(ConditionContext.class);
        doReturn(environmentMock).when(conditionContextMock).getEnvironment();
        doReturn(beanFactoryMock).when(conditionContextMock).getBeanFactory();
        doReturn(new LoggingReporter()).when(beanFactoryMock).getBean(CITRUS_HTML_REPORTER);

        HtmlReporterEnablementCondition condition = new HtmlReporterEnablementCondition();
        AnnotatedTypeMetadata annotatedTypeMetadataMock = mock(AnnotatedTypeMetadata.class);
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

        doReturn(null).when(beanFactoryMock).getBean(CITRUS_HTML_REPORTER);
        doReturn("false").when(environmentMock).getProperty(ReporterConfig.DEFAULT_HTML_REPORTER_ENABLED_PROPERTY, "true");
        Assert.assertFalse(condition.matches(conditionContextMock, annotatedTypeMetadataMock));

    }
}
