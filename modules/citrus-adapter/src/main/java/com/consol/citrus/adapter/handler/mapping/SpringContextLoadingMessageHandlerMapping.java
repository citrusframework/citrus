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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

/**
 * Message handler mapping loads new Spring Application contexts by one or more locations
 * and tries to find mappable Spring bean with given name or id.
 *
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class SpringContextLoadingMessageHandlerMapping implements MessageHandlerMapping {
    /** Application context configuration location holding available message handlers */
    protected String contextConfigLocation;

    /**
     * Finds message handler by mapping name.
     *
     * @param mappingName
     * @return
     */
    @Override
    public MessageHandler getMessageHandler(String mappingName) {
        //TODO support FileSystemContext
        Assert.notNull(contextConfigLocation, "Spring bean application context location must be set properly");

        ApplicationContext ctx = new ClassPathXmlApplicationContext(contextConfigLocation);
        MessageHandler handler;

        try {
            handler = ctx.getBean(mappingName, MessageHandler.class);
        } catch (NoSuchBeanDefinitionException e) {
            throw new CitrusRuntimeException("Unable to find matching message handler with bean name '" +
                    mappingName + "' in Spring bean application context", e);
        }

        return handler;
    }

    /**
     * Sets the context config location for building the Spring application context.
     * @param contextConfigLocation
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }
}
