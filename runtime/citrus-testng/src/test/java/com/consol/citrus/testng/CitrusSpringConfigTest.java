package com.consol.citrus.testng;

import com.consol.citrus.CitrusSpringSettings;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.functions.FunctionLibrary;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.log.DefaultLogModifier;
import com.consol.citrus.log.LogModifier;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.message.MessageProcessors;
import com.consol.citrus.report.FailureStackTestListener;
import com.consol.citrus.report.HtmlReporter;
import com.consol.citrus.report.JUnitReporter;
import com.consol.citrus.report.LoggingReporter;
import com.consol.citrus.report.MessageListener;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestActionListener;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.report.TestReporters;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.util.SpringBeanTypeConverter;
import com.consol.citrus.util.TypeConverter;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import com.consol.citrus.validation.matcher.ValidationMatcherLibrary;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@ContextConfiguration(classes = CitrusSpringConfigTest.CustomConfig.class)
public class CitrusSpringConfigTest extends TestNGCitrusSpringSupport {

    static {
        System.setProperty(CitrusSpringSettings.DEFAULT_APPLICATION_CONTEXT_PROPERTY, "classpath:citrus-unit-context.xml");
    }

    @Autowired
    private TestReporters testReporters;

    @Autowired
    private TestListeners testListeners;

    @Autowired
    private TestSuiteListeners testSuiteListeners;

    @Autowired
    private TestActionListeners testActionListeners;

    @Autowired
    private MessageListeners messageListeners;

    @Autowired
    private MessageProcessors messageProcessors;

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private ValidationMatcherRegistry validationMatcherRegistry;

    @Autowired
    private MessageValidatorRegistry messageValidatorRegistry;

    @Autowired
    private EndpointFactory endpointFactory;

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private TypeConverter typeConverter;

    @Autowired
    private LogModifier logModifier;

    @Autowired
    private TestContextFactoryBean testContextFactory;

    @Autowired
    private LoggingReporter loggingReporter;

    @Test
    public void verifySpringConfig() {
        Assert.assertEquals(testReporters.getTestReporters().size(), 4);
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(CustomConfig.reporter::equals));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(HtmlReporter.class::isInstance));
        Assert.assertTrue(testReporters.getTestReporters().stream().anyMatch(JUnitReporter.class::isInstance));

        Assert.assertEquals(testListeners.getTestListeners().size(), 5);
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(CustomConfig.testListener::equals));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(HtmlReporter.class::isInstance));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(FailureStackTestListener.class::isInstance));
        Assert.assertTrue(testListeners.getTestListeners().stream().anyMatch(TestReporters.class::isInstance));

        Assert.assertEquals(testSuiteListeners.getTestSuiteListeners().size(), 3);
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(CustomConfig.testSuiteListener::equals));
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(loggingReporter::equals));
        Assert.assertTrue(testSuiteListeners.getTestSuiteListeners().stream().anyMatch(TestReporters.class::isInstance));

        Assert.assertEquals(testActionListeners.getTestActionListeners().size(), 2);
        Assert.assertTrue(testActionListeners.getTestActionListeners().stream().anyMatch(CustomConfig.testActionListener::equals));
        Assert.assertTrue(testActionListeners.getTestActionListeners().stream().anyMatch(loggingReporter::equals));

        Assert.assertEquals(messageListeners.getMessageListener().size(), 2);
        Assert.assertTrue(messageListeners.getMessageListener().stream().anyMatch(CustomConfig.messageListener::equals));
        Assert.assertTrue(messageListeners.getMessageListener().stream().anyMatch(loggingReporter::equals));

        Assert.assertEquals(messageProcessors.getMessageProcessors().size(), 1);
        Assert.assertTrue(messageProcessors.getMessageProcessors().stream().anyMatch(CustomConfig.messageProcessor::equals));

        Assert.assertEquals(functionRegistry.getFunctionLibraries().size(), 2);
        Assert.assertTrue(functionRegistry.getFunctionLibraries().stream().anyMatch(CustomConfig.functionLibrary::equals));
        Assert.assertTrue(functionRegistry.getFunctionLibraries().stream().anyMatch(DefaultFunctionLibrary.class::isInstance));

        Assert.assertEquals(validationMatcherRegistry.getValidationMatcherLibraries().size(), 2);
        Assert.assertTrue(validationMatcherRegistry.getValidationMatcherLibraries().stream().anyMatch(CustomConfig.validationMatcherLibrary::equals));
        Assert.assertTrue(validationMatcherRegistry.getValidationMatcherLibraries().stream().anyMatch(DefaultValidationMatcherLibrary.class::isInstance));

        Assert.assertEquals(messageValidatorRegistry.getMessageValidators().size(), 2);
        Assert.assertTrue(messageValidatorRegistry.getMessageValidators().values().stream().anyMatch(CustomConfig.messageValidator::equals));
        Assert.assertTrue(messageValidatorRegistry.getMessageValidators().values().stream().anyMatch(DefaultMessageHeaderValidator.class::isInstance));

        Assert.assertEquals(endpointFactory.getClass(), DefaultEndpointFactory.class);

        Assert.assertEquals(referenceResolver.getClass(), SpringBeanReferenceResolver.class);
        Assert.assertEquals(typeConverter.getClass(), SpringBeanTypeConverter.class);
        Assert.assertEquals(logModifier.getClass(), DefaultLogModifier.class);

        Assert.assertEquals(testContextFactory.getTestListeners(), testListeners);
        Assert.assertEquals(testContextFactory.getMessageListeners(), messageListeners);
        Assert.assertEquals(testContextFactory.getMessageProcessors(), messageProcessors);
        Assert.assertEquals(testContextFactory.getFunctionRegistry(), functionRegistry);
        Assert.assertEquals(testContextFactory.getValidationMatcherRegistry(), validationMatcherRegistry);
        Assert.assertEquals(testContextFactory.getMessageValidatorRegistry(), messageValidatorRegistry);
        Assert.assertEquals(testContextFactory.getEndpointFactory(), endpointFactory);
        Assert.assertEquals(testContextFactory.getReferenceResolver(), referenceResolver);
        Assert.assertEquals(testContextFactory.getTypeConverter(), typeConverter);
        Assert.assertEquals(testContextFactory.getLogModifier(), logModifier);
    }

    @Configuration
    static class CustomConfig {
        static TestReporter reporter = Mockito.mock(TestReporter.class);
        static TestListener testListener = Mockito.mock(TestListener.class);
        static TestSuiteListener testSuiteListener = Mockito.mock(TestSuiteListener.class);
        static TestActionListener testActionListener = Mockito.mock(TestActionListener.class);
        static MessageListener messageListener = Mockito.mock(MessageListener.class);
        static MessageProcessor messageProcessor = Mockito.mock(MessageProcessor.class);

        static FunctionLibrary functionLibrary = Mockito.mock(FunctionLibrary.class);
        static ValidationMatcherLibrary validationMatcherLibrary = Mockito.mock(ValidationMatcherLibrary.class);
        static MessageValidator<?> messageValidator = Mockito.mock(MessageValidator.class);

        @Bean
        public TestReporter reporter() {
            return reporter;
        }

        @Bean
        public TestListener testListener() {
            return testListener;
        }

        @Bean
        public TestSuiteListener testSuiteListener() {
            return testSuiteListener;
        }

        @Bean
        public TestActionListener testActionListener() {
            return testActionListener;
        }

        @Bean
        public MessageListener messageListener() {
            return messageListener;
        }

        @Bean
        public MessageProcessor messageProcessor() {
            return messageProcessor;
        }

        @Bean
        public FunctionLibrary functionLibrary() {
            return functionLibrary;
        }

        @Bean
        public ValidationMatcherLibrary validationMatcherLibrary() {
            return validationMatcherLibrary;
        }

        @Bean
        public MessageValidator<?> messageValidator() {
            return messageValidator;
        }
    }
}
