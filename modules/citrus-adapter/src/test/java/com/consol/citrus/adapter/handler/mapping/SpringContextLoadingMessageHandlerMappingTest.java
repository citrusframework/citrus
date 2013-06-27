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
import org.easymock.EasyMock;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SpringContextLoadingMessageHandlerMappingTest {

    @Test
    public void testGetMessageHandler() throws Exception {
        SpringContextLoadingMessageHandlerMapping messageHandlerMapping = new SpringContextLoadingMessageHandlerMapping();
        messageHandlerMapping.setContextConfigLocation("com/consol/citrus/adapter/handler/test-context.xml");

        Assert.assertNotNull(messageHandlerMapping.getMessageHandler("EmptyResponseRequest"));
        Assert.assertNotNull(messageHandlerMapping.getMessageHandler("ContentResponseRequest"));

        try {
            messageHandlerMapping.getMessageHandler("Unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }
    }
}
