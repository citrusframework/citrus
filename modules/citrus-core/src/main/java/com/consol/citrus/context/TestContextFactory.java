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

package com.consol.citrus.context;

import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptors;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

/**
 * Factory bean implementation taking care of {@link FunctionRegistry} and {@link GlobalVariables}.
 * 
 * @author Christoph Deppisch
 */
public class TestContextFactory implements FactoryBean<TestContext>, ApplicationContextAware {
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
    @Autowired
    private ValidationMatcherRegistry validationMatcherRegistry;
    
    @Autowired(required = false)
    private GlobalVariables globalVariables = new GlobalVariables();
    
    @Autowired
    private MessageValidatorRegistry messageValidatorRegistry;

    @Autowired
    private TestListeners testListeners;

    @Autowired
    private MessageListeners messageListeners;

    @Autowired
    private EndpointFactory endpointFactory;

    @Autowired
    private ReferenceResolver referenceResolver;

    @Autowired
    private MessageConstructionInterceptors messageConstructionInterceptors;

    @Autowired(required=false)
    private NamespaceContextBuilder namespaceContextBuilder;

    /** Spring bean application context */
    private ApplicationContext applicationContext;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestContextFactory.class);

    /**
     * Construct new factory instance from application context.
     * @param applicationContext
     * @return
     */
    public static final TestContextFactory newInstance(ApplicationContext applicationContext) {
        TestContextFactory factory = new TestContextFactory();

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

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(MessageConstructionInterceptors.class))) {
            factory.setMessageConstructionInterceptors(applicationContext.getBean(MessageConstructionInterceptors.class));
        }

        if (!CollectionUtils.isEmpty(applicationContext.getBeansOfType(EndpointFactory.class))) {
            factory.setEndpointFactory(applicationContext.getBean(EndpointFactory.class));
        }

        factory.setApplicationContext(applicationContext);

        return factory;
    }
    
    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public TestContext getObject() {
        TestContext context = new TestContext();
        context.setFunctionRegistry(functionRegistry);
        context.setValidationMatcherRegistry(validationMatcherRegistry);
        context.setGlobalVariables(globalVariables);
        context.setMessageValidatorRegistry(messageValidatorRegistry);
        context.setTestListeners(testListeners);
        context.setMessageListeners(messageListeners);
        context.setMessageConstructionInterceptors(messageConstructionInterceptors);
        context.setEndpointFactory(endpointFactory);
        context.setReferenceResolver(referenceResolver);
        context.setApplicationContext(applicationContext);

        if (namespaceContextBuilder != null) {
            context.setNamespaceContextBuilder(namespaceContextBuilder);
        }

        if (log.isDebugEnabled()) {
            log.debug("Created new test context - using global variables: '"
                    + context.getGlobalVariables() + "'");
        }
        
        return context;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public Class getObjectType() {
        return TestContext.class;
    }

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
    public boolean isSingleton() {
        return false;
    }

    /**
     * @param functionRegistry the functionRegistry to set
     */
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * @return the functionRegistry
     */
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    /**
     * @param validationMatcherRegistry the validationMatcherRegistry to set
     */
    public void setValidationMatcherRegistry(
            ValidationMatcherRegistry validationMatcherRegistry) {
        this.validationMatcherRegistry = validationMatcherRegistry;
    }

    /**
     * @return the validationMatcherRegistry
     */
    public ValidationMatcherRegistry getValidationMatcherRegistry() {
        return validationMatcherRegistry;
    }

    /**
     * @param globalVariables the globalVariables to set
     */
    public void setGlobalVariables(GlobalVariables globalVariables) {
        this.globalVariables = globalVariables;
    }

    /**
     * @return the globalVariables
     */
    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    /**
     * Gets the endpoint factory.
     * @return
     */
    public EndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    /**
     * Sets the endpoint factory.
     * @param endpointFactory
     */
    public void setEndpointFactory(EndpointFactory endpointFactory) {
        this.endpointFactory = endpointFactory;
    }

    /**
     * Gets the value of the referenceResolver property.
     *
     * @return the referenceResolver
     */
    public ReferenceResolver getReferenceResolver() {
        return referenceResolver;
    }

    /**
     * Sets the referenceResolver property.
     *
     * @param referenceResolver
     */
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Sets the namespace context builder.
     * @param namespaceContextBuilder
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }

    /**
     * Gets the namespace context builder.
     * @return
     */
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return namespaceContextBuilder;
    }

    /**
     * Sets the test listeners.
     * @param testListeners
     */
    public void setTestListeners(TestListeners testListeners) {
        this.testListeners = testListeners;
    }

    /**
     * Gets the test listeners.
     * @return
     */
    public TestListeners getTestListeners() {
        return testListeners;
    }

    /**
     * Sets the message validator registry.
     * @param messageValidatorRegistry
     */
    public void setMessageValidatorRegistry(MessageValidatorRegistry messageValidatorRegistry) {
        this.messageValidatorRegistry = messageValidatorRegistry;
    }

    /**
     * Gets the message validator registry.
     * @return
     */
    public MessageValidatorRegistry getMessageValidatorRegistry() {
        return messageValidatorRegistry;
    }

    /**
     * Sets the message listeners.
     * @param messageListeners
     */
    public void setMessageListeners(MessageListeners messageListeners) {
        this.messageListeners = messageListeners;
    }

    /**
     * Gets the message listeners.
     * @return
     */
    public MessageListeners getMessageListeners() {
        return messageListeners;
    }

    /**
     * Sets the message construction interceptors.
     * @param messageConstructionInterceptors
     */
    public void setMessageConstructionInterceptors(MessageConstructionInterceptors messageConstructionInterceptors) {
        this.messageConstructionInterceptors = messageConstructionInterceptors;
    }

    /**
     * Gets the message construction interceptors.
     * @return
     */
    public MessageConstructionInterceptors getMessageConstructionInterceptors() {
        return messageConstructionInterceptors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
