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

package com.consol.citrus.dsl.handler;

import com.consol.citrus.TestCase;
import com.consol.citrus.adapter.handler.XmlTestExecutingMessageHandler;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.integration.Message;

/**
 * Test executing message handler specialization which executes a Java DSL test builder instead of
 * a Xml test case.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestExecutingMessageHandler extends XmlTestExecutingMessageHandler {

    @Override
    public Message<?> dispatchMessage(final Message<?> request, String mappingName) {
        final CitrusTestBuilder testBuilder;

        try {
            testBuilder = getApplicationContext().getBean(mappingName, CitrusTestBuilder.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        getTaskExecutor().execute(new Runnable() {
            public void run() {
                prepareExecution(request, testBuilder);
                testBuilder.execute();
            }
        });

        return getResponseMessageHandler().handleMessage(request);
    }

    @Override
    protected final void prepareExecution(Message<?> request, TestCase testCase) {
        super.prepareExecution(request, testCase);
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to teest builder
     * here.
     * @param request the triggering request message.
     * @param testBuilder the found test builder.
     */
    protected void prepareExecution(Message<?> request, CitrusTestBuilder testBuilder) {
    }
}
