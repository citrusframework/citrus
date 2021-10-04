package com.consol.citrus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.consol.citrus.annotations.CitrusConfiguration;
import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.BeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.DefaultFunctionRegistry;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.log.DefaultLogModifier;
import com.consol.citrus.log.LogModifier;
import com.consol.citrus.message.MessageProcessors;
import com.consol.citrus.report.DefaultTestReporters;
import com.consol.citrus.report.FailureStackTestListener;
import com.consol.citrus.report.MessageListener;
import com.consol.citrus.report.MessageListenerAware;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestActionListener;
import com.consol.citrus.report.TestActionListenerAware;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.report.TestListenerAware;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestReporter;
import com.consol.citrus.report.TestReporterAware;
import com.consol.citrus.report.TestReporters;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListenerAware;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.spi.BindToRegistry;
import com.consol.citrus.spi.ReferenceRegistry;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.SimpleReferenceResolver;
import com.consol.citrus.util.DefaultTypeConverter;
import com.consol.citrus.util.TypeConverter;
import com.consol.citrus.validation.DefaultMessageValidatorRegistry;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherRegistry;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

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
        try {
            parseConfiguration(configClass.getConstructor().newInstance());
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new CitrusRuntimeException("Missing or non-accessible default constructor on custom configuration class", e);
        }
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     * @param configuration
     */
    public void parseConfiguration(Object configuration) {
        Class<?> configClass = configuration.getClass();

        if (configClass.isAnnotationPresent(CitrusConfiguration.class)) {
            for (Class<?> type : configClass.getAnnotation(CitrusConfiguration.class).classes()) {
                parseConfiguration(type);
            }
        }

        Arrays.stream(configClass.getDeclaredMethods())
                .filter(m -> m.getAnnotation(BindToRegistry.class) != null)
                .forEach(m -> {
                    try {
                        String name = ReferenceRegistry.getName(m.getAnnotation(BindToRegistry.class), m.getName());
                        Object component = m.invoke(configuration);
                        getReferenceResolver().bind(name, component);

                        if (component instanceof MessageValidator) {
                            getMessageValidatorRegistry().addMessageValidator(name, (MessageValidator<? extends ValidationContext>) component);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new CitrusRuntimeException("Failed to invoke configuration method", e);
                    }
                });

        Arrays.stream(configClass.getDeclaredFields())
                .filter(f -> f.getAnnotation(BindToRegistry.class) != null)
                .peek(ReflectionUtils::makeAccessible)
                .forEach(f -> {
                    try {
                        String name = ReferenceRegistry.getName(f.getAnnotation(BindToRegistry.class), f.getName());
                        Object component = f.get(configuration);
                        getReferenceResolver().bind(name, component);

                        if (component instanceof MessageValidator) {
                            getMessageValidatorRegistry().addMessageValidator(name, (MessageValidator<? extends ValidationContext>) component);
                        }
                    } catch (IllegalAccessException e) {
                        throw new CitrusRuntimeException("Failed to access configuration field", e);
                    }
                });
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
        private TypeConverter typeConverter = new DefaultTypeConverter();
        private LogModifier logModifier = new DefaultLogModifier();

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
