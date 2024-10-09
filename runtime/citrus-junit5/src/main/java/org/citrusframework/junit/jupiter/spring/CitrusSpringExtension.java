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

package org.citrusframework.junit.jupiter.spring;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusSpringContext;
import org.citrusframework.CitrusSpringContextProvider;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.junit.jupiter.CitrusExtension;
import org.citrusframework.junit.jupiter.CitrusExtensionHelper;
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

import java.lang.reflect.Method;

import static org.citrusframework.junit.jupiter.CitrusExtensionHelper.requiresCitrus;
import static org.citrusframework.junit.jupiter.CitrusExtensionHelper.setCitrus;
import static org.springframework.test.context.junit.jupiter.SpringExtension.getApplicationContext;

/**
 * JUnit5 extension adding {@link TestCaseRunner} support as well as Citrus annotation based resource injection
 * and lifecycle management such as before/after suite.
 * <p>
 * Extension resolves method parameter of type {@link org.citrusframework.context.TestContext}, {@link TestCaseRunner}
 * or {@link org.citrusframework.TestActionRunner} and injects endpoints and resources coming from Citrus Spring application context that
 * is automatically loaded at suite start up. After suite automatically includes Citrus report generation.
 * <p>
 * Extension is based on Citrus Xml test extension that also allows to load test cases from external Spring configuration files.
 *
 */
public class CitrusSpringExtension implements BeforeAllCallback, BeforeTestExecutionCallback, InvocationInterceptor, AfterTestExecutionCallback, ParameterResolver, TestInstancePostProcessor, TestExecutionExceptionHandler, AfterEachCallback {

    private Citrus citrus;
    private ApplicationContext applicationContext;
    private final CitrusExtension delegate = new CitrusExtension();

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        setCitrus(getCitrus(extensionContext), extensionContext);
        delegate.beforeAll(extensionContext);
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
        setCitrus(getCitrus(extensionContext), extensionContext);
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
     *
     * @param extensionContext
     * @return
     */
    protected Citrus getCitrus(ExtensionContext extensionContext) {
        ApplicationContext ctx = getApplicationContext(extensionContext);

        Citrus existing = null;
        if (!requiresCitrus(extensionContext)) {
            existing = CitrusExtensionHelper.getCitrus(extensionContext);
        }

        if (applicationContext == null || !applicationContext.equals(ctx)) {
            applicationContext = ctx;
        }

        if (citrus == null) {
            if (existing != null && existing.getCitrusContext() instanceof CitrusSpringContext citrusSpringContext
                    && citrusSpringContext.getApplicationContext().equals(applicationContext)) {
                citrus = existing;
            } else {
                citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));

                if (existing != null) {
                    citrus.getCitrusContext().handleTestResults(existing.getCitrusContext().getTestResults());
                }
            }
        } else if (existing == null
                || !(existing.getCitrusContext() instanceof CitrusSpringContext citrusSpringContext)
                || !citrusSpringContext.getApplicationContext().equals(applicationContext)) {
            citrus = Citrus.newInstance(new CitrusSpringContextProvider(applicationContext));
        }

        return citrus;
    }
}
