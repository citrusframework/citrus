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

package com.consol.citrus.adapter.handler;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactoryBean;
import com.consol.citrus.dsl.CitrusTestBuilder;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Message dispatching message handler invokes test builder instance execution. Provides
 * response message and delegates to respective test builder instance for further testing
 * logic executed in separate thread.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class TestExecutingMessageHandler extends XpathDispatchingMessageHandler implements InitializingBean {

    /** Executor start action sequence logic in separate thread task */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /** Spring bean application context holding all available test builders and basic Citrus config */
    private ApplicationContext applicationContext;

    @Override
    protected Message<?> dispatchMessage(Message<?> request, String mappingName) {
        final CitrusTestBuilder testBuilder;

        try {
            testBuilder = applicationContext.getBean(mappingName, CitrusTestBuilder.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find test builder with name '" +
                    mappingName + "' in Spring bean context", e);
        }

        if (testBuilder != null) {
            taskExecutor.execute(new Runnable() {
                public void run() {
                    testBuilder.execute();
                }
            });

            return MessageBuilder.withPayload("OK").build();
        } else {
            throw new CitrusRuntimeException("Could not find test builder bean with name '" +
                    mappingName + "' in '" + messageHandlerContext + "'");
        }
    }

    @Override
    protected String extractMappingName(Node matchingNode) {
        if (StringUtils.hasText(matchingNode.getNodeValue())) {
            return matchingNode.getNodeValue();
        } else {
            return super.extractMappingName(matchingNode);
        }
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
        // Create Citrus application context with custom messageHandlerContext
        applicationContext = new ClassPathXmlApplicationContext(new String[] {
                "classpath:com/consol/citrus/spring/root-application-ctx.xml",
                "classpath:com/consol/citrus/functions/citrus-function-ctx.xml",
                "classpath:com/consol/citrus/validation/citrus-validationmatcher-ctx.xml",
                messageHandlerContext });
    }
}
