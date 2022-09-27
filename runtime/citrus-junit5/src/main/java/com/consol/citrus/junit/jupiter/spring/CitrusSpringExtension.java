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

package com.consol.citrus.junit.jupiter.spring;

import java.lang.reflect.Method;

import com.consol.citrus.Citrus;
import com.consol.citrus.CitrusSpringContextProvider;
import com.consol.citrus.TestCaseRunner;
import com.consol.citrus.junit.jupiter.CitrusExtension;
import com.consol.citrus.junit.jupiter.CitrusExtensionHelper;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 *
 * Extension resolves method parameter of type {@link com.consol.citrus.context.TestContext}, {@link TestCaseRunner}
 * or {@link com.consol.citrus.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 *
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 * @author Christoph Deppisch
 */
public class CitrusSpringExtension implements BeforeAllCallback, BeforeTestExecutionCallback, InvocationInterceptor,
        AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor, TestExecutionExceptionHandler, AfterEachCallback, AfterAllCallback {

    private Citrus citrus;
    private ApplicationContext applicationContext;
    private final CitrusExtension delegate = new CitrusExtension();

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        CitrusExtensionHelper.setCitrus(getCitrus(extensionContext), extensionContext);
        delegate.beforeAll(extensionContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        delegate.afterAll(extensionContext);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        delegate.handleTestExecutionException(extensionContext, throwable);
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) {
        delegate.afterTestExecution(extensionContext);
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) {
        CitrusExtensionHelper.setCitrus(getCitrus(extensionContext), extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        delegate.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        delegate.afterEach(extensionContext);
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) {
        delegate.postProcessTestInstance(testInstance, extensionContext);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return delegate.supportsParameter(parameterContext, extensionContext);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return delegate.resolveParameter(parameterContext, extensionContext);
    }

    /**
     * Create Citrus instance if not set already. Use SpringExtension to load application context.
     * @param extensionContext
     * @return
     */
    protected Citrus getCitrus(ExtensionContext extensionContext) {
        ApplicationContext ctx = SpringExtension.getApplicationContext(extensionContext);
        if (applicationContext == null) {
            applicationContext = ctx;
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(ctx));
        } else if (!applicationContext.equals(ctx)) {
            applicationContext = ctx;
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(ctx));
        }

        return citrus;
    }
}
