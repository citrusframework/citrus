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
public class SpringBeanMessageHandlerMappingTest {

    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    private MessageHandler fooMessageHandler = EasyMock.createMock(MessageHandler.class);
    private MessageHandler barMessageHandler = EasyMock.createMock(MessageHandler.class);

    @Test
    public void testGetMessageHandler() throws Exception {
        SpringBeanMessageHandlerMapping messageHandlerMapping = new SpringBeanMessageHandlerMapping();

        messageHandlerMapping.setApplicationContext(applicationContext);

        reset(applicationContext);

        expect(applicationContext.getBean("foo", MessageHandler.class)).andReturn(fooMessageHandler).once();
        expect(applicationContext.getBean("bar", MessageHandler.class)).andReturn(barMessageHandler).once();
        expect(applicationContext.getBean("unknown", MessageHandler.class)).andThrow(new NoSuchBeanDefinitionException("unknown")).once();

        replay(applicationContext);

        Assert.assertEquals(messageHandlerMapping.getMessageHandler("foo"), fooMessageHandler);
        Assert.assertEquals(messageHandlerMapping.getMessageHandler("bar"), barMessageHandler);

        try {
            messageHandlerMapping.getMessageHandler("unknown");
            Assert.fail("Missing exception due to unknown mapping key");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof NoSuchBeanDefinitionException);
        }

        verify(applicationContext);
    }
}
