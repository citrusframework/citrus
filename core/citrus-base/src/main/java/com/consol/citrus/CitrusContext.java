package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.container.AfterSuite;
import com.consol.citrus.container.BeforeSuite;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.DefaultFunctionRegistry;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestListener;
import com.consol.citrus.report.TestListenerAware;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListener;
import com.consol.citrus.report.TestSuiteListenerAware;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.spi.ReferenceRegistry;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.SimpleReferenceResolver;
import com.consol.citrus.validation.DefaultMessageValidatorRegistry;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.interceptor.GlobalMessageConstructionInterceptors;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;

/**
 * Default Citrus context implementation holds basic components used in Citrus.
 *
 * @author Christoph Deppisch
 */
public class CitrusContext implements TestListenerAware, TestSuiteListenerAware, ReferenceRegistry {

    /** Test context factory **/
    private final TestContextFactory testContextFactory;
    private final TestSuiteListeners testSuiteListeners;
    private final TestListeners testListeners;

    private final List<BeforeSuite> beforeSuite;
    private final List<AfterSuite> afterSuite;

    private final FunctionRegistry functionRegistry;
    private final ValidationMatcherRegistry validationMatcherRegistry;
    private final GlobalVariables globalVariables;
    private final MessageValidatorRegistry messageValidatorRegistry;
    private final MessageListeners messageListeners;
    private final EndpointFactory endpointFactory;
    private final ReferenceResolver referenceResolver;
    private final GlobalMessageConstructionInterceptors globalMessageConstructionInterceptors;
    private final NamespaceContextBuilder namespaceContextBuilder;

    /**
     * Protected constructor using given builder to construct this instance.
     * @param builder the instance builder.
     */
    protected CitrusContext(Builder builder) {
        this.testSuiteListeners = builder.testSuiteListeners;
        this.testListeners = builder.testListeners;

        this.beforeSuite = builder.beforeSuite;
        this.afterSuite = builder.afterSuite;

        this.functionRegistry = builder.functionRegistry;
        this.validationMatcherRegistry = builder.validationMatcherRegistry;
        this.globalVariables = builder.globalVariables;
        this.messageValidatorRegistry = builder.messageValidatorRegistry;
        this.messageListeners = builder.messageListeners;
        this.endpointFactory = builder.endpointFactory;
        this.referenceResolver = builder.referenceResolver;
        this.globalMessageConstructionInterceptors = builder.globalMessageConstructionInterceptors;
        this.namespaceContextBuilder = builder.namespaceContextBuilder;

        this.testContextFactory = Optional.ofNullable(builder.testContextFactory)
                .orElseGet(TestContextFactory::newInstance);
    }

    /**
     * Initializing method loads Spring application context and reads bean definitions
     * such as test listeners and test context factory.
     * @return
     */
    public static CitrusContext create() {
        return new CitrusContext(new Builder());
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
     * Obtains the globalMessageConstructionInterceptors.
     * @return
     */
    public GlobalMessageConstructionInterceptors getGlobalMessageConstructionInterceptors() {
        return globalMessageConstructionInterceptors;
    }

    /**
     * Obtains the namespaceContextBuilder.
     * @return
     */
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return namespaceContextBuilder;
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
        if (this.referenceResolver instanceof ReferenceRegistry) {
            ((ReferenceRegistry) this.referenceResolver).bind(name, value);
        }
    }

    /**
     * Citrus context builder.
     */
    public static class Builder {
        private TestContextFactory testContextFactory;
        private TestSuiteListeners testSuiteListeners = new TestSuiteListeners();
        private TestListeners testListeners = new TestListeners();

        private List<BeforeSuite> beforeSuite = new ArrayList<>();
        private List<AfterSuite> afterSuite = new ArrayList<>();

        private FunctionRegistry functionRegistry = new DefaultFunctionRegistry();
        private ValidationMatcherRegistry validationMatcherRegistry = new ValidationMatcherRegistry();
        private GlobalVariables globalVariables = new GlobalVariables();
        private MessageValidatorRegistry messageValidatorRegistry = new DefaultMessageValidatorRegistry();
        private MessageListeners messageListeners = new MessageListeners();
        private EndpointFactory endpointFactory = new DefaultEndpointFactory();
        private ReferenceResolver referenceResolver = new SimpleReferenceResolver();
        private GlobalMessageConstructionInterceptors globalMessageConstructionInterceptors = new GlobalMessageConstructionInterceptors();
        private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

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

        public Builder endpointFactory(EndpointFactory endpointFactory) {
            this.endpointFactory = endpointFactory;
            return this;
        }

        public Builder referenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return this;
        }

        public Builder globalMessageConstructionInterceptors(GlobalMessageConstructionInterceptors globalMessageConstructionInterceptors) {
            this.globalMessageConstructionInterceptors = globalMessageConstructionInterceptors;
            return this;
        }

        public Builder namespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
            this.namespaceContextBuilder = namespaceContextBuilder;
            return this;
        }

        public CitrusContext build() {
            return new CitrusContext(this);
        }
    }
}
