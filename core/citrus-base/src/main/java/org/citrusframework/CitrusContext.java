package org.citrusframework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.citrusframework.annotations.CitrusAnnotations;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.container.AfterSuite;
import org.citrusframework.container.AfterTest;
import org.citrusframework.container.BeforeSuite;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.DefaultEndpointFactory;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.DefaultFunctionRegistry;
import org.citrusframework.functions.FunctionLibrary;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.DefaultLogModifier;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.report.*;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.DefaultMessageValidatorRegistry;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.matcher.DefaultValidationMatcherRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherLibrary;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;

/**
 * Default Citrus context implementation holds basic components used in Citrus.
 *
 * @author Christoph Deppisch
 */
public class CitrusContext implements TestListenerAware, TestActionListenerAware,
        TestSuiteListenerAware, TestReporterAware, MessageListenerAware, ReferenceRegistry {

    /** Test context factory **/
    private final TestContextFactory testContextFactory;
    private final TestSuiteListeners testSuiteListeners;
    private final TestListeners testListeners;
    private final TestActionListeners testActionListeners;
    private final TestReporters testReporters;

    private final List<BeforeSuite> beforeSuite;
    private final List<AfterSuite> afterSuite;

    private final FunctionRegistry functionRegistry;
    private final ValidationMatcherRegistry validationMatcherRegistry;
    private final GlobalVariables globalVariables;
    private final MessageValidatorRegistry messageValidatorRegistry;
    private final MessageListeners messageListeners;
    private final EndpointFactory endpointFactory;
    private final ReferenceResolver referenceResolver;
    private final MessageProcessors messageProcessors;
    private final NamespaceContextBuilder namespaceContextBuilder;
    private final TypeConverter typeConverter;
    private final LogModifier logModifier;

    private final Set<Class<?>> configurationClasses = new HashSet<>();

    /**
     * Protected constructor using given builder to construct this instance.
     * @param builder the instance builder.
     */
    protected CitrusContext(Builder builder) {
        this.testSuiteListeners = builder.testSuiteListeners;
        this.testListeners = builder.testListeners;
        this.testActionListeners = builder.testActionListeners;
        this.testReporters = builder.testReporters;

        this.beforeSuite = builder.beforeSuite;
        this.afterSuite = builder.afterSuite;

        this.functionRegistry = builder.functionRegistry;
        this.validationMatcherRegistry = builder.validationMatcherRegistry;
        this.globalVariables = builder.globalVariables;
        this.messageValidatorRegistry = builder.messageValidatorRegistry;
        this.messageListeners = builder.messageListeners;
        this.endpointFactory = builder.endpointFactory;
        this.referenceResolver = builder.referenceResolver;
        this.messageProcessors = builder.messageProcessors;
        this.namespaceContextBuilder = builder.namespaceContextBuilder;
        this.typeConverter = builder.typeConverter;
        this.logModifier = builder.logModifier;

        this.testContextFactory = builder.testContextFactory;

        builder.configurationClasses.forEach(this::parseConfiguration);
    }

    /**
     * Initializing method loads default configuration class and reads component definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static CitrusContext create() {
        CitrusContext context = Builder.defaultContext().build();

        if (StringUtils.hasText(CitrusSettings.DEFAULT_CONFIG_CLASS)) {
            try {
                Class<?> configClass = Class.forName(CitrusSettings.DEFAULT_CONFIG_CLASS);
                context.parseConfiguration(configClass);
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Failed to instantiate custom configuration class", e);
            }
        }

        return context;
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     * @param configClass
     */
    public void parseConfiguration(Class<?> configClass) {
        if (configurationClasses.contains(configClass)) {
            return;
        }

        configurationClasses.add(configClass);
        CitrusAnnotations.parseConfiguration(configClass, this);
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     * @param configuration
     */
    public void parseConfiguration(Object configuration) {
        if (configurationClasses.contains(configuration.getClass())) {
            return;
        }

        configurationClasses.add(configuration.getClass());
        CitrusAnnotations.parseConfiguration(configuration, this);
    }

    /**
     * Creates a new test context.
     * @return the new citrus test context.
     */
    public TestContext createTestContext() {
        return testContextFactory.getObject();
    }

    @Override
    public void addTestSuiteListener(TestSuiteListener suiteListener) {
        this.testSuiteListeners.addTestSuiteListener(suiteListener);
    }

    @Override
    public void addTestListener(TestListener testListener) {
        this.testListeners.addTestListener(testListener);
    }


    @Override
    public void addTestActionListener(TestActionListener testActionListener) {
        this.testActionListeners.addTestActionListener(testActionListener);
    }

    @Override
    public void addTestReporter(TestReporter testReporter) {
        this.testReporters.addTestReporter(testReporter);
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        this.messageListeners.addMessageListener(listener);
    }

    /**
     * Closes the context and all its components.
     */
    public void close() {
    }

    /**
     * Gets list of after suite actions in this context.
     * @return
     */
    public List<AfterSuite> getAfterSuite() {
        return afterSuite;
    }

    /**
     * Gets list of before suite actions in this context.
     * @return
     */
    public List<BeforeSuite> getBeforeSuite() {
        return beforeSuite;
    }

    /**
     * Gets test listeners in this context.
     * @return
     */
    public TestListeners getTestListeners() {
        return testListeners;
    }


    /**
     * Gets the test action listeners in this context.
     * @return
     */
    public TestActionListeners getTestActionListeners() {
        return testActionListeners;
    }

    /**
     * Gets test suite listeners in this context.
     * @return
     */
    public TestSuiteListeners getTestSuiteListeners() {
        return testSuiteListeners;
    }

    /**
     * Obtains the functionRegistry.
     * @return
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }

    /**
     * Obtains the validationMatcherRegistry.
     * @return
     */
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return validationMatcherRegistry;
    }

    /**
     * Obtains the globalVariables.
     * @return
     */
    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    /**
     * Obtains the messageValidatorRegistry.
     * @return
     */
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        return messageValidatorRegistry;
    }

    /**
     * Obtains the messageListeners.
     * @return
     */
    public MessageListeners getMessageListeners() {
        return messageListeners;
    }

    /**
     * Obtains the endpointFactory.
     * @return
     */
    public EndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    /**
     * Obtains the referenceResolver.
     * @return
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    /**
     * Obtains the messageProcessors.
     * @return
     */
    public MessageProcessors getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Obtains the namespaceContextBuilder.
     * @return
     */
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return namespaceContextBuilder;
    }

    /**
     * Obtains the typeConverter.
     * @return
     */
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    /**
     * Gets the logModifier.
     * @return
     */
    public LogModifier getLogModifier() {
        return logModifier;
    }

    /**
     * Obtains the testContextFactory.
     * @return
     */
    public TestContextFactory getTestContextFactory() {
        return testContextFactory;
    }

    @Override
    public void bind(String name, Object value) {
        if (this.referenceResolver != null) {
            this.referenceResolver.bind(name, value);

            if (value instanceof MessageValidator) {
                getMessageValidatorRegistry().addMessageValidator(name, (MessageValidator<? extends ValidationContext>) value);
            }
        }
    }

    public TestResults getTestResults() {
        return testReporters.getTestResults();
    }

    public void handleTestResults(TestResults testResults) {
        if (!getTestResults().equals(testResults)) {
            testResults.doWithResults(result -> getTestResults().addResult(result));
        }
    }

    public void addComponent(String name, Object component) {
        if (component instanceof InitializingPhase c) {
            c.initialize();
        }
        referenceResolver.bind(name, component);

        if (component instanceof MessageValidator<?> messageValidator) {
            messageValidatorRegistry.addMessageValidator(name, messageValidator);
            testContextFactory.getMessageValidatorRegistry().addMessageValidator(name, messageValidator);
        }

        if (component instanceof MessageProcessor messageProcessor) {
            messageProcessors.addMessageProcessor(messageProcessor);
            testContextFactory.getMessageProcessors().addMessageProcessor(messageProcessor);
        }

        if (component instanceof TestSuiteListener suiteListener) {
            testSuiteListeners.addTestSuiteListener(suiteListener);
        }

        if (component instanceof TestListener testListener) {
            testListeners.addTestListener(testListener);
            testContextFactory.getTestListeners().addTestListener(testListener);
        }

        if (component instanceof TestReporter testReporter) {
            testReporters.addTestReporter(testReporter);
        }

        if (component instanceof TestActionListener testActionListener) {
            testActionListeners.addTestActionListener(testActionListener);
            testContextFactory.getTestActionListeners().addTestActionListener(testActionListener);
        }

        if (component instanceof MessageListener messageListener) {
            messageListeners.addMessageListener(messageListener);
            testContextFactory.getMessageListeners().addMessageListener(messageListener);
        }

        if (component instanceof BeforeTest beforeTest) {
            testContextFactory.getBeforeTest().add(beforeTest);
        }

        if (component instanceof AfterTest afterTest) {
            testContextFactory.getAfterTest().add(afterTest);
        }

        if (component instanceof BeforeSuite beforeSuiteComponent) {
            beforeSuite.add(beforeSuiteComponent);
        }

        if (component instanceof AfterSuite afterSuiteComponent) {
            afterSuite.add(afterSuiteComponent);
        }

        if (component instanceof FunctionLibrary library) {
            functionRegistry.addFunctionLibrary(library);
            testContextFactory.getFunctionRegistry().addFunctionLibrary(library);
        }

        if (component instanceof ValidationMatcherLibrary library) {
            validationMatcherRegistry.addValidationMatcherLibrary(library);
            testContextFactory.getValidationMatcherRegistry().addValidationMatcherLibrary(library);
        }
    }

    /**
     * Citrus context builder.
     */
    public static class Builder {
        private TestContextFactory testContextFactory;
        private TestSuiteListeners testSuiteListeners = new TestSuiteListeners();
        private TestListeners testListeners = new TestListeners();
        private TestActionListeners testActionListeners = new TestActionListeners();
        private TestReporters testReporters = new DefaultTestReporters();

        private final List<BeforeSuite> beforeSuite = new ArrayList<>();
        private final List<AfterSuite> afterSuite = new ArrayList<>();

        private FunctionRegistry functionRegistry = new DefaultFunctionRegistry();
        private ValidationMatcherRegistry validationMatcherRegistry = new DefaultValidationMatcherRegistry();
        private GlobalVariables globalVariables = new GlobalVariables();
        private MessageValidatorRegistry messageValidatorRegistry = new DefaultMessageValidatorRegistry();
        private MessageListeners messageListeners = new MessageListeners();
        private EndpointFactory endpointFactory = new DefaultEndpointFactory();
        private ReferenceResolver referenceResolver = new SimpleReferenceResolver();
        private MessageProcessors messageProcessors = new MessageProcessors();
        private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
        private TypeConverter typeConverter = TypeConverter.lookupDefault();
        private LogModifier logModifier = new DefaultLogModifier();

        private final Set<Class<?>> configurationClasses = new LinkedHashSet<>();

        public static Builder defaultContext() {
            Builder builder = new Builder();

            builder.testReporters
                    .getTestReporters()
                    .stream()
                    .filter(TestSuiteListener.class::isInstance)
                    .map(TestSuiteListener.class::cast)
                    .forEach(builder::testSuiteListener);
            builder.testSuiteListener(builder.testReporters);

            builder.testReporters
                    .getTestReporters()
                    .stream()
                    .filter(TestListener.class::isInstance)
                    .map(TestListener.class::cast)
                    .forEach(builder::testListener);
            builder.testListener(builder.testReporters);
            builder.testListener(new FailureStackTestListener());

            builder.testReporters
                    .getTestReporters()
                    .stream()
                    .filter(TestActionListener.class::isInstance)
                    .map(TestActionListener.class::cast)
                    .forEach(builder::testActionListener);

            builder.testReporters
                    .getTestReporters()
                    .stream()
                    .filter(MessageListener.class::isInstance)
                    .map(MessageListener.class::cast)
                    .forEach(builder::messageListener);
            return builder;
        }

        public Builder testContextFactory(TestContextFactory testContextFactory) {
            this.testContextFactory = testContextFactory;
            return this;
        }

        public Builder testSuiteListeners(TestSuiteListeners testSuiteListeners) {
            this.testSuiteListeners = testSuiteListeners;
            return this;
        }

        public Builder testSuiteListener(TestSuiteListener testSuiteListener) {
            this.testSuiteListeners.addTestSuiteListener(testSuiteListener);
            return this;
        }

        public Builder testListeners(TestListeners testListeners) {
            this.testListeners = testListeners;
            return this;
        }

        public Builder testListener(TestListener testListener) {
            this.testListeners.addTestListener(testListener);
            return this;
        }

        public Builder testActionListeners(TestActionListeners testActionListeners) {
            this.testActionListeners = testActionListeners;
            return this;
        }

        public Builder testActionListener(TestActionListener testActionListener) {
            this.testActionListeners.addTestActionListener(testActionListener);
            return this;
        }

        public Builder testReporters(TestReporters testReporters) {
            this.testReporters = testReporters;
            return this;
        }

        public Builder testReporter(TestReporter testReporter) {
            this.testReporters.addTestReporter(testReporter);
            return this;
        }

        public Builder beforeSuite(List<BeforeSuite> beforeSuite) {
            this.beforeSuite.addAll(beforeSuite);
            return this;
        }

        public Builder beforeSuite(BeforeSuite beforeSuite) {
            this.beforeSuite.add(beforeSuite);
            return this;
        }

        public Builder afterSuite(List<AfterSuite> afterSuite) {
            this.afterSuite.addAll(afterSuite);
            return this;
        }

        public Builder afterSuite(AfterSuite afterSuite) {
            this.afterSuite.add(afterSuite);
            return this;
        }

        public Builder functionRegistry(FunctionRegistry functionRegistry) {
            this.functionRegistry = functionRegistry;
            return this;
        }

        public Builder validationMatcherRegistry(ValidationMatcherRegistry validationMatcherRegistry) {
            this.validationMatcherRegistry = validationMatcherRegistry;
            return this;
        }

        public Builder globalVariables(GlobalVariables globalVariables) {
            this.globalVariables = globalVariables;
            return this;
        }

        public Builder messageValidatorRegistry(MessageValidatorRegistry messageValidatorRegistry) {
            this.messageValidatorRegistry = messageValidatorRegistry;
            return this;
        }

        public Builder messageListeners(MessageListeners messageListeners) {
            this.messageListeners = messageListeners;
            return this;
        }

        public Builder messageListener(MessageListener messageListeners) {
            this.messageListeners.addMessageListener(messageListeners);
            return this;
        }

        public Builder endpointFactory(EndpointFactory endpointFactory) {
            this.endpointFactory = endpointFactory;
            return this;
        }

        public Builder referenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        public Builder messageProcessors(MessageProcessors messageProcessors) {
            this.messageProcessors = messageProcessors;
            return this;
        }

        public Builder namespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
            this.namespaceContextBuilder = namespaceContextBuilder;
            return this;
        }

        public Builder typeConverter(TypeConverter converter) {
            this.typeConverter = converter;
            return this;
        }

        public Builder logModifier(LogModifier modifier) {
            this.logModifier = modifier;
            return this;
        }

        public Builder loadConfiguration(Class<?> configClass) {
            this.configurationClasses.add(configClass);
            return this;
        }

        public CitrusContext build() {
            if (testContextFactory == null) {
                testContextFactory = TestContextFactory.newInstance();

                testContextFactory.setFunctionRegistry(this.functionRegistry);
                testContextFactory.setValidationMatcherRegistry(this.validationMatcherRegistry);
                testContextFactory.setGlobalVariables(this.globalVariables);
                testContextFactory.setMessageValidatorRegistry(this.messageValidatorRegistry);
                testContextFactory.setTestListeners(this.testListeners);
                testContextFactory.setTestActionListeners(this.testActionListeners);
                testContextFactory.setMessageListeners(this.messageListeners);
                testContextFactory.setMessageProcessors(this.messageProcessors);
                testContextFactory.setEndpointFactory(this.endpointFactory);
                testContextFactory.setReferenceResolver(this.referenceResolver);
                testContextFactory.setNamespaceContextBuilder(this.namespaceContextBuilder);
                testContextFactory.setTypeConverter(this.typeConverter);
                testContextFactory.setLogModifier(this.logModifier);
            }

            return new CitrusContext(this);
        }
    }
}
