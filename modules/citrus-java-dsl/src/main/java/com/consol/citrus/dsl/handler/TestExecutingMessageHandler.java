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

import com.consol.citrus.adapter.handler.MessageChannelConnectingMessageHandler;
import com.consol.citrus.adapter.handler.RequestDispatchingMessageHandler;
import com.consol.citrus.adapter.handler.mapping.SpringBeanMessageHandlerMapping;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.server.AbstractServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;

/**
 * Message dispatching message handler triggers test builder instance execution for each incoming request.
 * Delegates to respective test builder instance by bean name mapping for further testing logic executed in
 * separate thread.
 *
 * First response message is handle by separate response message handler. Usually this is some message channel or
 * jms connecting message handler so first response message is also delegated to test builder logic.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestExecutingMessageHandler extends RequestDispatchingMessageHandler implements InitializingBean, BeanNameAware, ApplicationContextAware {

    /** Executor start action sequence logic in separate thread task */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /** This handlers name - used for message channel generation */
    private String name = TestExecutingMessageHandler.class.getSimpleName();

    /** Spring bean application context holding all available test builders and basic Citrus config */
    private ApplicationContext applicationContext;

    /** First response message is handled by this message handler - handler can also delegate to test case */
    private MessageHandler responseMessageHandler;

    @Override
    public Message<?> dispatchMessage(final Message<?> request, String mappingName) {
        final CitrusTestBuilder testBuilder;

        try {
            testBuilder = applicationContext.getBean(mappingName, CitrusTestBuilder.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        taskExecutor.execute(new Runnable() {
            public void run() {
                prepareExecution(request, testBuilder);
                testBuilder.execute();
            }
        });

        return responseMessageHandler.handleMessage(request);
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to teest builder
     * here.
     * @param request the triggering request message.
     * @param testBuilder the found test builder.
     */
    protected void prepareExecution(Message<?> request, CitrusTestBuilder testBuilder) {
    }

    /**
     * Sets the task executor. Usually some async task executor for test execution in
     * separate thread instance.
     *
     * @param taskExecutor
     */
    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * Creates Citrus Spring bean application context with basic beans and settings for Citrus. Custom
     * messageHandlerContext should hold various test builder beans for later dispatching and test execution.
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (responseMessageHandler == null) {
            MessageChannelConnectingMessageHandler channelConnectingMessageHandler = new MessageChannelConnectingMessageHandler();
            channelConnectingMessageHandler.setChannelName(name + AbstractServer.DEFAULT_CHANNEL_ID_SUFFIX);
            channelConnectingMessageHandler.setBeanFactory(applicationContext);
            responseMessageHandler = channelConnectingMessageHandler;
        }

        if (getMessageHandlerMapping() == null) {
            SpringBeanMessageHandlerMapping messageHandlerMapping = new SpringBeanMessageHandlerMapping();
            messageHandlerMapping.setApplicationContext(applicationContext);
            setMessageHandlerMapping(messageHandlerMapping);
        }
    }

    /**
     * Injects this handlers bean name.
     * @param name
     */
    public void setBeanName(String name) {
        this.name = name;
    }

    /**
     * Sets the response message handler delegate.
     * @param responseMessageHandler
     */
    public void setResponseMessageHandler(MessageHandler responseMessageHandler) {
        this.responseMessageHandler = responseMessageHandler;
    }

    /**
     * Injects Spring bean application context this handler is managed by.
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
