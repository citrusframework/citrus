/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.citrus.dsl.endpoint;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.dsl.design.TestDesigner;
import org.citrusframework.citrus.dsl.runner.ExecutableTestRunnerComponent;
import org.citrusframework.citrus.dsl.runner.TestRunner;
import org.citrusframework.citrus.endpoint.adapter.XmlTestExecutingEndpointAdapter;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.message.Message;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

/**
 * Test executing endpoint adapter specialization executes a Java DSL test designer or test runner loaded from
 * Spring application context by bean name mapping.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestExecutingEndpointAdapter extends XmlTestExecutingEndpointAdapter {

    @Override
    public Message dispatchMessage(final Message request, String mappingName) {
        final Executable executable;

        try {
            executable = getApplicationContext().getBean(mappingName, Executable.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        getTaskExecutor().execute(() -> {
            if (executable instanceof TestRunner) {
                prepareExecution(request, (TestRunner) executable);
                if (executable instanceof ExecutableTestRunnerComponent) {
                    ((ExecutableTestRunnerComponent) executable).prepareExecution();
                }
            } else if (executable instanceof TestDesigner) {
                prepareExecution(request, (TestDesigner) executable);
            }

            executable.execute();
        });

        return getResponseEndpointAdapter().handleMessage(request);
    }

    @Override
    protected final void prepareExecution(Message request, TestCase testCase) {
        super.prepareExecution(request, testCase);
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to test builder
     * here.
     * @param request the triggering request message.
     * @param testDesigner the found test builder.
     */
    protected void prepareExecution(Message request, TestDesigner testDesigner) {
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to test builder
     * here.
     * @param request the triggering request message.
     * @param testDesigner the found test builder.
     */
    protected void prepareExecution(Message request, TestRunner testDesigner) {
    }
}
