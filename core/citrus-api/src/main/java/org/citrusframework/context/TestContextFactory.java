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

package org.citrusframework.context;

import java.util.List;
import java.util.ServiceLoader;

import org.citrusframework.api.container.AfterTest;
import org.citrusframework.api.container.BeforeTest;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.SegmentVariableExtractorRegistry;

public interface TestContextFactory extends ReferenceResolverAware {

    /**
     * Factory method creates new test context instance and adds all default components in this factory.
     */
    TestContext getObject();

    static TestContextFactory newInstance() {
        // The loader finds first factory registered on the classpath/modulepath
        return ServiceLoader.load(TestContextFactory.class).findFirst()
                .orElseThrow(() -> new CitrusRuntimeException("No TestContextFactory registered on the classpath/modulepath"));
    }

    static TestContext copyOf(TestContext context) {
        TestContext result = new TestContext();
        result.setFunctionRegistry(context.getFunctionRegistry());

        result.setGlobalVariables(new GlobalVariables.Builder()
                .variables(context.getGlobalVariables())
                .build());
        result.getVariables().putAll(context.getVariables());

        result.setMessageStore(context.getMessageStore());
        result.setMessageValidatorRegistry(context.getMessageValidatorRegistry());
        result.setValidationMatcherRegistry(context.getValidationMatcherRegistry());
        result.setTestListeners(context.getTestListeners());
        result.setMessageListeners(context.getMessageListeners());
        result.setMessageProcessors(context.getMessageProcessors());
        result.setEndpointFactory(context.getEndpointFactory());
        result.setNamespaceContextBuilder(context.getNamespaceContextBuilder());
        result.setReferenceResolver(context.getReferenceResolver());
        result.setTypeConverter(context.getTypeConverter());
        result.setLogModifier(context.getLogModifier());
        return result;
    }

    void setFunctionRegistry(FunctionRegistry functionRegistry);

    FunctionRegistry getFunctionRegistry();

    void setValidationMatcherRegistry(ValidationMatcherRegistry validationMatcherRegistry);

    ValidationMatcherRegistry getValidationMatcherRegistry();

    void setGlobalVariables(GlobalVariables globalVariables);

    GlobalVariables getGlobalVariables();

    EndpointFactory getEndpointFactory();

    void setEndpointFactory(EndpointFactory endpointFactory);

    ReferenceResolver getReferenceResolver();

    void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder);

    NamespaceContextBuilder getNamespaceContextBuilder();

    void setTestListeners(TestListeners testListeners);

    TestListeners getTestListeners();

    TestActionListeners getTestActionListeners();

    void setTestActionListeners(TestActionListeners testActionListeners);

    List<BeforeTest> getBeforeTest();

    void setBeforeTest(List<BeforeTest> beforeTest);

    List<AfterTest> getAfterTest();

    void setAfterTest(List<AfterTest> afterTest);

    void setMessageValidatorRegistry(MessageValidatorRegistry messageValidatorRegistry);

    MessageValidatorRegistry getMessageValidatorRegistry();

    void setMessageListeners(MessageListeners messageListeners);

    MessageListeners getMessageListeners();

    void setMessageProcessors(MessageProcessors messageProcessors);

    MessageProcessors getMessageProcessors();

    TypeConverter getTypeConverter();

    void setTypeConverter(TypeConverter typeConverter);

    LogModifier getLogModifier();

    void setLogModifier(LogModifier logModifier);

    SegmentVariableExtractorRegistry getSegmentVariableExtractorRegistry();

    void setSegmentVariableExtractorRegistry(SegmentVariableExtractorRegistry segmentVariableExtractorRegistry);
}
