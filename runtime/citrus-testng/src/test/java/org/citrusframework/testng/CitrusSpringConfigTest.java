package org.citrusframework.testng;

import org.citrusframework.CitrusSpringSettings;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContextFactoryBean;
import org.citrusframework.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.FunctionLibrary;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.DefaultLogModifier;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.report.FailureStackTestListener;
import org.citrusframework.report.HtmlReporter;
import org.citrusframework.report.JUnitReporter;
import org.citrusframework.report.LoggingReporter;
import org.citrusframework.report.MessageListener;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListener;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListener;
import org.citrusframework.report.TestListeners;
import org.citrusframework.report.TestReporter;
import org.citrusframework.report.TestReporters;
import org.citrusframework.report.TestSuiteListener;
import org.citrusframework.report.TestSuiteListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SpringBeanTypeConverter;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.DefaultValidationMatcherLibrary;
import org.citrusframework.validation.matcher.ValidationMatcherLibrary;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
        @Mock
        static TestReporter reporter;
        @Mock
        static TestListener testListener;
        @Mock
        static TestSuiteListener testSuiteListener;
        @Mock
        static TestActionListener testActionListener;
        @Mock
        static MessageListener messageListener;
        @Mock
        static MessageProcessor messageProcessor;

        @Mock
        static FunctionLibrary functionLibrary;
        @Mock
        static ValidationMatcherLibrary validationMatcherLibrary;
        @Mock
        static MessageValidator<?> messageValidator;

        public CustomConfig() {
            MockitoAnnotations.openMocks(this);
        }

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
