/*
 * Copyright 2006-2023 the original author or authors.
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

package org.citrusframework.endpoint.adapter;

import org.citrusframework.TestBehavior;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.TestCaseRunnerFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Test executing endpoint adapter specialization executes a Java DSL test designer or test runner loaded from
 * Spring application context by bean name mapping.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestBehaviorExecutingEndpointAdapter extends XmlTestExecutingEndpointAdapter {

    @Override
    public Message dispatchMessage(final Message request, String mappingName) {
        final TestBehavior behavior;

        try {
            behavior = getApplicationContext().getBean(mappingName, TestBehavior.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test behavior with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        getTaskExecutor().execute(() -> {
            prepareExecution(request, behavior);
            TestContext context = getTestContext();
            TestCaseRunner testCaseRunner = TestCaseRunnerFactory.createRunner(context);
            behavior.apply(testCaseRunner);
        });

        return getResponseEndpointAdapter().handleMessage(request);
    }

    /**
     * Prepares the test behavior instance before execution. Subclasses may add custom properties to test behavior
     * here.
     * @param request the triggering request message.
     * @param behavior the found test behavior.
     */
    protected void prepareExecution(Message request, TestBehavior behavior) {
    }

    @Override
    protected final void prepareExecution(Message request, TestCase testCase) {
        super.prepareExecution(request, testCase);
    }
}
