/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
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
