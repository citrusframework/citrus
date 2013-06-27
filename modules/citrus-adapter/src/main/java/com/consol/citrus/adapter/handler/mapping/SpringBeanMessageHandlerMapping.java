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

package com.consol.citrus.adapter.handler.mapping;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * Message handler mapping is Spring application context aware and tries to find appropriate Spring bean in
 * context for the mapping name.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SpringBeanMessageHandlerMapping implements MessageHandlerMapping, ApplicationContextAware {
    /** Application context holding available message handlers */
    protected ApplicationContext applicationContext;

    private String beanNamePrefix = "";
    private String beanNameSuffix = "";

    /**
     * Finds message handler by mapping name.
     *
     * @param mappingName
     * @return
     */
    @Override
    public MessageHandler getMessageHandler(String mappingName) {
        MessageHandler handler;

        try {
            handler = applicationContext.getBean(beanNamePrefix + mappingName + beanNameSuffix, MessageHandler.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find matching message handler with bean name '" +
                    mappingName + "' in Spring bean application context", e);
        }

        return handler;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the bean name prefix used when resolving bean names.
     * @param beanNamePrefix
     */
    public void setBeanNamePrefix(String beanNamePrefix) {
        this.beanNamePrefix = beanNamePrefix;
    }

    /**
     * Sets the bean name suffix used when resolving bean names.
     * @param beanNameSuffix
     */
    public void setBeanNameSuffix(String beanNameSuffix) {
        this.beanNameSuffix = beanNameSuffix;
    }
}
