/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.context;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.container.AfterTest;
import org.citrusframework.container.BeforeTest;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.LogModifier;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNullApi;
import org.springframework.util.CollectionUtils;

/**
 * Factory bean implementation taking care of {@link FunctionRegistry} and {@link GlobalVariables}. Enriches a test context factory delegate with
 * components coming from Spring application context. In addition to that adds application context reference to the test context when building new instances.
 *
 * @author Christoph Deppisch
 */
public class TestContextFactoryBean extends TestContextFactory implements FactoryBean<TestContext>, InitializingBean, ApplicationContextAware {

    @Autowired
    private FunctionRegistry functionRegistry;

    @Autowired
    private ValidationMatcherRegistry validationMatcherRegistry;

    @Autowired(required = false)
    private GlobalVariables globalVariables;

    @Autowired
    private MessageValidatorRegistry messageValidatorRegistry;

    @Autowired
    private TestListeners testListeners;

    @Autowired
    private TestActionListeners testActionListeners;

    @Autowired(required = false)
    private List<BeforeTest> beforeTest = new ArrayList<>();

    @Autowired(required = false)
    private List<AfterTest> afterTest = new ArrayList<>();

    @Autowired
    private MessageListeners messageListeners;

    @Autowired
    private EndpointFactory endpointFactory;

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private TypeConverter typeConverter;

    @Autowired
    private LogModifier logModifier;

    @Autowired
    private MessageProcessors messageProcessors;

    @Autowired(required=false)
    private NamespaceContextBuilder namespaceContextBuilder;

    @Autowired
    private SegmentVariableExtractorRegistry segmentVariableExtractorRegistry;

    /** Spring bean application context that created this factory */
    private ApplicationContext applicationContext;

    /** The context factory delegate */
    private final TestContextFactory delegate;

    /**
     * Default constructor using default factory delegate.
     */
    public TestContextFactoryBean() {
        this(TestContextFactory.newInstance());
    }

    /**
     * Constructor initializes with given factory delegate.
     * @param factory
     */
    public TestContextFactoryBean(TestContextFactory factory) {
        this.delegate = factory;
    }

    /**
     * Construct new factory instance from application context.
     * @param applicationContext
     * @return
     */
    public static TestContextFactory newInstance(ApplicationContext applicationContext) {
        TestContextFactory factory = TestContextFactory.newInstance();

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(FunctionRegistry.class))) {
            factory.setFunctionRegistry(applicationContext.getBean(FunctionRegistry.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ValidationMatcherRegistry.class))) {
            factory.setValidationMatcherRegistry(applicationContext.getBean(ValidationMatcherRegistry.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(GlobalVariables.class))) {
            factory.setGlobalVariables(applicationContext.getBean(GlobalVariables.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(MessageValidatorRegistry.class))) {
            factory.setMessageValidatorRegistry(applicationContext.getBean(MessageValidatorRegistry.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(TestListeners.class))) {
            factory.setTestListeners(applicationContext.getBean(TestListeners.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(MessageListeners.class))) {
            factory.setMessageListeners(applicationContext.getBean(MessageListeners.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(MessageProcessors.class))) {
            factory.setMessageProcessors(applicationContext.getBean(MessageProcessors.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(EndpointFactory.class))) {
            factory.setEndpointFactory(applicationContext.getBean(EndpointFactory.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(ReferenceResolver.class))) {
            factory.setReferenceResolver(applicationContext.getBean(ReferenceResolver.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(TypeConverter.class))) {
            factory.setTypeConverter(applicationContext.getBean(TypeConverter.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(LogModifier.class))) {
            factory.setLogModifier(applicationContext.getBean(LogModifier.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(NamespaceContextBuilder.class))) {
            factory.setNamespaceContextBuilder(applicationContext.getBean(NamespaceContextBuilder.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(SegmentVariableExtractorRegistry.class))) {
            factory.setSegmentVariableExtractorRegistry(applicationContext.getBean(SegmentVariableExtractorRegistry.class));
        }

        return factory;
    }

    @Override
    public Class<TestContext> getObjectType() {
        return TestContext.class;
    }

    @Override
    public TestContext getObject() {
        return delegate.getObject();
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (functionRegistry != null) {
            delegate.setFunctionRegistry(functionRegistry);
        }

        if (validationMatcherRegistry != null) {
            delegate.setValidationMatcherRegistry(validationMatcherRegistry);
        }

        if (globalVariables != null) {
            delegate.setGlobalVariables(globalVariables);
        }

        if (messageValidatorRegistry != null) {
            delegate.setMessageValidatorRegistry(messageValidatorRegistry);
        }

        if (testListeners != null) {
            delegate.setTestListeners(testListeners);
        }

        if (testActionListeners != null) {
            delegate.setTestActionListeners(testActionListeners);
        }

        if (beforeTest != null) {
            delegate.setBeforeTest(beforeTest);
        }

        if (afterTest != null) {
            delegate.setAfterTest(afterTest);
        }

        if (messageListeners != null) {
            delegate.setMessageListeners(messageListeners);
        }

        if (messageProcessors != null) {
            delegate.setMessageProcessors(messageProcessors);
        }

        if (endpointFactory != null) {
            delegate.setEndpointFactory(endpointFactory);
        }

        if (referenceResolver != null) {
            delegate.setReferenceResolver(referenceResolver);
        }

        if (typeConverter != null) {
            delegate.setTypeConverter(typeConverter);
        }

        if (logModifier != null) {
            delegate.setLogModifier(logModifier);
        }

        if (namespaceContextBuilder != null) {
            delegate.setNamespaceContextBuilder(namespaceContextBuilder);
        }

        if (segmentVariableExtractorRegistry != null) {
            delegate.setSegmentVariableExtractorRegistry(segmentVariableExtractorRegistry);
        }
    }

    /**
     * Obtains the applicationContext.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public FunctionRegistry getFunctionRegistry() {
        return delegate.getFunctionRegistry();
    }

    @Override
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return delegate.getValidationMatcherRegistry();
    }

    @Override
    public GlobalVariables getGlobalVariables() {
        return delegate.getGlobalVariables();
    }

    @Override
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        return delegate.getMessageValidatorRegistry();
    }

    @Override
    public TestListeners getTestListeners() {
        return delegate.getTestListeners();
    }

    @Override
    public TestActionListeners getTestActionListeners() {
        return delegate.getTestActionListeners();
    }

    @Override
    public List<BeforeTest> getBeforeTest() {
        return delegate.getBeforeTest();
    }

    @Override
    public List<AfterTest> getAfterTest() {
        return delegate.getAfterTest();
    }

    @Override
    public MessageListeners getMessageListeners() {
        return delegate.getMessageListeners();
    }

    @Override
    public MessageProcessors getMessageProcessors() {
        return delegate.getMessageProcessors();
    }

    @Override
    public EndpointFactory getEndpointFactory() {
        return delegate.getEndpointFactory();
    }

    @Override
    public ReferenceResolver getReferenceResolver() {
        return delegate.getReferenceResolver();
    }

    @Override
    public TypeConverter getTypeConverter() {
        return delegate.getTypeConverter();
    }

    @Override
    public LogModifier getLogModifier() {
        return delegate.getLogModifier();
    }

    @Override
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return delegate.getNamespaceContextBuilder();
    }

    @Override
    public SegmentVariableExtractorRegistry getSegmentVariableExtractorRegistry() {
        return delegate.getSegmentVariableExtractorRegistry();
    }

}
