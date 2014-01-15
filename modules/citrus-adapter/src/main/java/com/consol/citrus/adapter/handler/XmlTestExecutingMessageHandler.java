/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.TestCase;
import com.consol.citrus.adapter.handler.mapping.SpringBeanMessageHandlerMapping;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;

/**
 * Special request dispatching message handler invokes Xml test case for each incoming message. Incoming message is
 * passed to test case via normal message channel connection as usual.
 *
 * @author Christoph Deppisch
 */
public class XmlTestExecutingMessageHandler extends RequestDispatchingMessageHandler implements InitializingBean, BeanNameAware, ApplicationContextAware {
    /** Executor start action sequence logic in separate thread task */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /** This handlers name - used for message channel generation */
    private String name = MessageHandler.class.getSimpleName();

    /** Spring bean application context holding all available test builders and basic Citrus config */
    private ApplicationContext applicationContext;

    /** First request message is handled by this message handler */
    private MessageHandler responseMessageHandler;

    /** Default package to search for Xml test case files */
    private String packageName = "com.consol.citrus.tests";

    @Override
    public Message<?> dispatchMessage(final Message<?> request, String mappingName) {
        final TestCase test;
        final TestContext testContext;

        try {
            testContext = applicationContext.getBean(TestContext.class);
            test = getTestCase(testContext, mappingName);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        taskExecutor.execute(new Runnable() {
            public void run() {
                prepareExecution(request, test);
                test.execute(testContext);
            }
        });

        return responseMessageHandler.handleMessage(request);
    }

    /**
     * Gets the test case from application context.
     * @param context
     * @param testName
     * @return the new test case.
     */
    protected TestCase getTestCase(TestContext context, String testName) {
        ClassPathXmlApplicationContext ctx = createApplicationContext(context, packageName, testName);

        try {
            TestCase testCase = ctx.getBean(testName, TestCase.class);
            testCase.setPackageName(packageName);
            return testCase;
        } catch (NoSuchBeanDefinitionException e) {
            throw context.handleError(testName, packageName, "Could not find test with name '" + testName + "'", e);
        }
    }

    /**
     * Creates the Spring application context.
     * @return
     */
    protected ClassPathXmlApplicationContext createApplicationContext(TestContext context, String packageName, String testName) {
        try {
            return new ClassPathXmlApplicationContext(
                    new String[] {
                            packageName.replace('.', '/') + "/" + testName + ".xml",
                            "com/consol/citrus/spring/internal-helper-ctx.xml"},
                    true, applicationContext);
        } catch (Exception e) {
            throw context.handleError(getClass().getSimpleName(), getClass().getPackage().getName(), "Failed to load test case", e);
        }
    }

    /**
     * Prepares the test builder instance before execution. Subclasses may add custom properties to teest builder
     * here.
     * @param request the triggering request message.
     * @param testCase the found test builder.
     */
    protected void prepareExecution(Message<?> request, TestCase testCase) {
    }

    /**
     * Creates Citrus Spring bean application context with basic beans and settings for Citrus. Custom
     * messageHandlerContext should hold various test builder beans for later dispatching and test execution.
     * @throws Exception
     */
    public void afterPropertiesSet() throws Exception {
        if (responseMessageHandler == null) {
            MessageChannelConnectingMessageHandler channelConnectingMessageHandler = new MessageChannelConnectingMessageHandler();
            channelConnectingMessageHandler.setChannelName(name + ".inbound");
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
     * Gets default test case package.
     * @return
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets default test case package.
     * @param packageName
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the task executor.
     * @return
     */
    public TaskExecutor getTaskExecutor() {
        return taskExecutor;
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
     * Gets the response message handler delegate.
     * @return
     */
    public MessageHandler getResponseMessageHandler() {
        return responseMessageHandler;
    }

    /**
     * Sets the response message handler delegate.
     * @param responseMessageHandler
     */
    public void setResponseMessageHandler(MessageHandler responseMessageHandler) {
        this.responseMessageHandler = responseMessageHandler;
    }

    /**
     * Gets the application context.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Injects Spring bean application context this handler is managed by.
     * @param applicationContext
     * @throws org.springframework.beans.BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
