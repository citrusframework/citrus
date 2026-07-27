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

package org.citrusframework;

import java.util.List;
import java.util.ServiceLoader;

import org.citrusframework.api.container.AfterSuite;
import org.citrusframework.api.container.BeforeSuite;
import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.EndpointFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.FunctionRegistry;
import org.citrusframework.log.LogModifier;
import org.citrusframework.message.MessageProcessors;
import org.citrusframework.report.MessageListenerAware;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.report.TestActionListenerAware;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.report.TestListenerAware;
import org.citrusframework.report.TestListeners;
import org.citrusframework.report.TestReporterAware;
import org.citrusframework.report.TestResults;
import org.citrusframework.report.TestSuiteListenerAware;
import org.citrusframework.report.TestSuiteListeners;
import org.citrusframework.spi.ReferenceRegistry;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.TypeConverter;
import org.citrusframework.validation.MessageValidatorRegistry;
import org.citrusframework.validation.matcher.ValidationMatcherRegistry;
import org.citrusframework.variable.GlobalVariables;

public interface CitrusContext extends TestListenerAware, TestActionListenerAware,
        TestSuiteListenerAware, TestReporterAware, MessageListenerAware, ReferenceRegistry {

    static CitrusContext create() {
        CitrusContext context = ServiceLoader.load(CitrusContext.class).findFirst()
                .orElseThrow(() -> new CitrusRuntimeException("No CitrusContext registered on the classpath/modulepath"));;

        if (StringUtils.hasText(CitrusSettings.DEFAULT_CONFIG_CLASS)) {
            try {
                Class<?> configClass = Class.forName(CitrusSettings.DEFAULT_CONFIG_CLASS, true, ClassLoaderHelper.getClassLoader());
                context.parseConfiguration(configClass);
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException("Failed to instantiate custom configuration class", e);
            }
        }

        return context;
    }

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     */
    void parseConfiguration(Class<?> configClass);

    /**
     * Parse given configuration class and bind annotated fields, methods to reference registry.
     */
    void parseConfiguration(Object configuration);

    /**
     * Creates a new test context.
     * @return the new citrus test context.
     */
    TestContext createTestContext();

    /**
     * Closes the context and all its components.
     */
    void close();

    /**
     * Gets test listeners in this context.
     */
    TestListeners getTestListeners();

    /**
     * Gets the test action listeners in this context.
     */
    TestActionListeners getTestActionListeners();

    /**
     * Gets test suite listeners in this context.
     */
    TestSuiteListeners getTestSuiteListeners();

    /**
     * Gets list of after suite actions in this context.
     */
    List<AfterSuite> getAfterSuite();

    /**
     * Gets list of before suite actions in this context.
     */
    List<BeforeSuite> getBeforeSuite();

    /**
     * Obtains the functionRegistry.
     */
    FunctionRegistry getFunctionRegistry();

    /**
     * Obtains the validationMatcherRegistry.
     */
    ValidationMatcherRegistry getValidationMatcherRegistry();

    /**
     * Obtains the globalVariables.
     */
    GlobalVariables getGlobalVariables();

    /**
     * Obtains the messageValidatorRegistry.
     */
    MessageValidatorRegistry getMessageValidatorRegistry();

    /**
     * Obtains the messageListeners.
     */
    MessageListeners getMessageListeners();

    /**
     * Obtains the endpointFactory.
     */
    EndpointFactory getEndpointFactory();

    /**
     * Obtains the referenceResolver.
     */
    ReferenceResolver getReferenceResolver();

    /**
     * Obtains the messageProcessors.
     */
    MessageProcessors getMessageProcessors();

    /**
     * Obtains the namespaceContextBuilder.
     */
    NamespaceContextBuilder getNamespaceContextBuilder();

    /**
     * Obtains the typeConverter.
     */
    TypeConverter getTypeConverter();

    /**
     * Gets the logModifier.
     */
    LogModifier getLogModifier();

    /**
     * Obtains the testContextFactory.
     */
    TestContextFactory getTestContextFactory();

    TestResults getTestResults();

    void handleTestResults(TestResults testResults);

    void addComponent(String name, Object component);
}
