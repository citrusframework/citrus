/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.context;

import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.endpoint.DefaultEndpointFactory;
import com.consol.citrus.endpoint.EndpointFactory;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.log.DefaultLogModifier;
import com.consol.citrus.log.LogModifier;
import com.consol.citrus.message.MessageProcessors;
import com.consol.citrus.report.MessageListeners;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.spi.SimpleReferenceResolver;
import com.consol.citrus.util.TypeConverter;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.matcher.ValidationMatcherRegistry;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.SegmentVariableExtractorRegistry;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;

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

    private TestActionListeners testActionListeners;

    private List<BeforeTest> beforeTest = new ArrayList<>();

    private List<AfterTest> afterTest = new ArrayList<>();

    private MessageListeners messageListeners;

    private EndpointFactory endpointFactory;

    private ReferenceResolver referenceResolver;

    private MessageProcessors messageProcessors;

    private NamespaceContextBuilder namespaceContextBuilder;

    private TypeConverter typeConverter;

    private LogModifier logModifier;

    private SegmentVariableExtractorRegistry segmentVariableExtractorRegistry;

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
        factory.setTestActionListeners(new TestActionListeners());
        factory.setMessageListeners(new MessageListeners());
        factory.setMessageProcessors(new MessageProcessors());
        factory.setEndpointFactory(new DefaultEndpointFactory());
        factory.setReferenceResolver(new SimpleReferenceResolver());
        factory.setNamespaceContextBuilder(new NamespaceContextBuilder());
        factory.setTypeConverter(TypeConverter.lookupDefault());
        factory.setLogModifier(new DefaultLogModifier());
        factory.setSegmentVariableExtractorRegistry(new SegmentVariableExtractorRegistry());

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
        context.setTestActionListeners(testActionListeners);
        context.setBeforeTest(beforeTest);
        context.setAfterTest(afterTest);
        context.setMessageListeners(messageListeners);
        context.setMessageProcessors(messageProcessors);
        context.setEndpointFactory(endpointFactory);
        context.setReferenceResolver(referenceResolver);
        context.setSegmentVariableExtractorRegistry(segmentVariableExtractorRegistry);

        if (namespaceContextBuilder != null) {
            context.setNamespaceContextBuilder(namespaceContextBuilder);
        }

        if (typeConverter != null) {
            context.setTypeConverter(typeConverter);
        }

        if (logModifier != null) {
            context.setLogModifier(logModifier);
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
     * Obtains the testActionListeners.
     * @return
     */
    public TestActionListeners getTestActionListeners() {
        return testActionListeners;
    }

    /**
     * Specifies the testActionListeners.
     * @param testActionListeners
     */
    public void setTestActionListeners(TestActionListeners testActionListeners) {
        this.testActionListeners = testActionListeners;
    }

    /**
     * Obtains the beforeTest.
     * @return
     */
    public List<BeforeTest> getBeforeTest() {
        return beforeTest;
    }

    /**
     * Specifies the beforeTest.
     * @param beforeTest
     */
    public void setBeforeTest(List<BeforeTest> beforeTest) {
        this.beforeTest = beforeTest;
    }

    /**
     * Obtains the afterTest.
     * @return
     */
    public List<AfterTest> getAfterTest() {
        return afterTest;
    }

    /**
     * Specifies the afterTest.
     * @param afterTest
     */
    public void setAfterTest(List<AfterTest> afterTest) {
        this.afterTest = afterTest;
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
     * Sets the message processors.
     * @param messageProcessors
     */
    public void setMessageProcessors(MessageProcessors messageProcessors) {
        this.messageProcessors = messageProcessors;
    }

    /**
     * Gets the message processors.
     * @return
     */
    public MessageProcessors getMessageProcessors() {
        return messageProcessors;
    }

    /**
     * Obtains the typeConverter.
     * @return
     */
    public TypeConverter getTypeConverter() {
        return typeConverter;
    }

    /**
     * Specifies the typeConverter.
     * @param typeConverter
     */
    public void setTypeConverter(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }

    /**
     * Gets the logModifier.
     * @return
     */
    public LogModifier getLogModifier() {
        return logModifier;
    }

    /**
     * Sets the logModifier.
     * @param logModifier
     */
    public void setLogModifier(LogModifier logModifier) {
        this.logModifier = logModifier;
    }

    /**
     * Gets the segmentVariableExtractorRegistry
     * @return
     */
    public SegmentVariableExtractorRegistry getSegmentVariableExtractorRegistry() {
        return segmentVariableExtractorRegistry;
    }

    /**
     * Sets the segmentVariableExtractorRegistry
     * @param segmentVariableExtractorRegistry
     */
    public void setSegmentVariableExtractorRegistry(SegmentVariableExtractorRegistry segmentVariableExtractorRegistry) {
        this.segmentVariableExtractorRegistry = segmentVariableExtractorRegistry;
    }

}
