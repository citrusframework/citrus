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

import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.spi.SimpleReferenceResolver;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.interceptor.MessageConstructionInterceptors;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory bean implementation constructs test context instances. Takes care of adding proper default components
 * to the test context such as {@link FunctionRegistry} or {@link GlobalVariables}.
 *
 * @author Christoph Deppisch
 */
public class TestContextFactory implements ReferenceResolverAware {

    private FunctionRegistry functionRegistry;

    private ValidationMatcherRegistry validationMatcherRegistry;

    private GlobalVariables globalVariables = new GlobalVariables();

    private MessageValidatorRegistry messageValidatorRegistry;

    private TestListeners testListeners;

    private MessageListeners messageListeners;

    private EndpointFactory endpointFactory;

    private ReferenceResolver referenceResolver;

    private MessageConstructionInterceptors messageConstructionInterceptors;

    private NamespaceContextBuilder namespaceContextBuilder;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TestContextFactory.class);

    /**
     * Create new empty instance with default components set.
     * @return
     */
    public static TestContextFactory newInstance() {
        TestContextFactory factory = new TestContextFactory();

        factory.setFunctionRegistry(new FunctionRegistry());
        factory.setValidationMatcherRegistry(new ValidationMatcherRegistry());
        factory.setGlobalVariables(new GlobalVariables());
        factory.setMessageValidatorRegistry(new MessageValidatorRegistry());
        factory.setTestListeners(new TestListeners());
        factory.setMessageListeners(new MessageListeners());
        factory.setMessageConstructionInterceptors(new MessageConstructionInterceptors());
        factory.setEndpointFactory(new DefaultEndpointFactory());
        factory.setReferenceResolver(new SimpleReferenceResolver());
        factory.setNamespaceContextBuilder(new NamespaceContextBuilder());

        return factory;
    }

    /**
     * Factory method creates new test context instance and adds all default components in this factory.
     * @return
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

    @Override
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
}
