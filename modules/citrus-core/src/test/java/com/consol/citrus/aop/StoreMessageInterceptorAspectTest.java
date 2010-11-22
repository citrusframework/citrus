/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class StoreMessageInterceptorAspectTest extends AbstractBaseTest {
    @Autowired
    StoreMessageInterceptorAspect storageAspect;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        if(new FileSystemResource("logs/debug/messages/message" + 1 + ".body").exists()) {
            new FileSystemResource("logs/debug/messages/message" + 1 + ".body").getFile().delete();
        }
        
        if(new FileSystemResource("logs/debug/messages/message" + 1 + ".header").exists()) {
            new FileSystemResource("logs/debug/messages/message" + 1 + ".header").getFile().delete();
        }
        
        StoreMessageInterceptorAspect.resetFileCounter();
    }
    
    @Test
    public void testStoreMessage() {
        Message<?> message = MessageBuilder.withPayload("<message>"
                                    + "<text>Hello TestFramework</text>"
                                  + "</message>").setHeader("operation", "greeting").build();
        
        storageAspect.doInterceptMessage(message);
        
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/message" + 1 + ".body").exists());
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/message" + 1 + ".header").exists());
    }
    
    @Test
    public void testStoreMessageWithoutHeader() {
        Message<?> message = MessageBuilder.withPayload("<message>"
                                    + "<text>Hello TestFramework</text>"
                                  + "</message>").build();
        
        storageAspect.doInterceptMessage(message);
        
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/message" + 1 + ".body").exists());
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/message" + 1 + ".header").exists());
    }
}
