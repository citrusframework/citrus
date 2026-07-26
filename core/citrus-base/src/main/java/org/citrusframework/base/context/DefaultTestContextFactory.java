/*
 * Copyright the original author or authors.
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

package org.citrusframework.base.context;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.api.container.AfterTest;
import org.citrusframework.api.container.BeforeTest;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.base.endpoint.DefaultEndpointFactory;
import org.citrusframework.base.functions.DefaultFunctionRegistry;
import org.citrusframework.base.validation.matcher.DefaultValidationMatcherRegistry;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.DefaultLogModifier;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.DefaultMessageValidatorRegistry;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;

/**
 * Factory bean implementation constructs test context instances. Takes care of adding proper default components
 * to the test context such as {@link FunctionRegistry} or {@link GlobalVariables}.
 *
 */
public class DefaultTestContextFactory implements TestContextFactory {

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

    public DefaultTestContextFactory() {
        this.functionRegistry = new DefaultFunctionRegistry();
        this.validationMatcherRegistry = new DefaultValidationMatcherRegistry();
        this.globalVariables = new GlobalVariables();
        this.messageValidatorRegistry = new DefaultMessageValidatorRegistry();
        this.testListeners = new TestListeners();
        this.testActionListeners = new TestActionListeners();
        this.messageListeners = new MessageListeners();
        this.messageProcessors = new MessageProcessors();
        this.endpointFactory = new DefaultEndpointFactory();
        this.referenceResolver = new SimpleReferenceResolver();
        this.namespaceContextBuilder = new NamespaceContextBuilder();
        this.typeConverter = TypeConverter.lookupDefault();
        this.logModifier = new DefaultLogModifier();
        this.segmentVariableExtractorRegistry = new SegmentVariableExtractorRegistry();
    }

    /**
     * Factory method creates new test context instance and adds all default components in this factory.
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
