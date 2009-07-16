package com.consol.citrus.aop;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.message.XMLMessage;

public class StoreMessageInterceptorAspectTest extends AbstractBaseTest {
    @Autowired
    StoreMessageInterceptorAspect storageAspect;
    
    @Test
    public void testStoreMessage() {
        XMLMessage message = new XMLMessage();
        
        message.setHeader(Collections.singletonMap("operation", "greeting"));
        
        message.setMessagePayload("<message>"
                                    + "<text>Hello TestFramework</text>"
                                  + "</message>");
        
        storageAspect.doInterceptMessage(message);
        
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/" + message.toString() + ".body").exists());
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/" + message.toString() + ".header").exists());
    }
    
    @Test
    public void testStoreMessageWithoutHeader() {
        XMLMessage message = new XMLMessage();
        
        message.setMessagePayload("<message>"
                                    + "<text>Hello TestFramework</text>"
                                  + "</message>");
        
        storageAspect.doInterceptMessage(message);
        
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/" + message.toString() + ".body").exists());
        Assert.assertTrue(new FileSystemResource("logs/debug/messages/" + message.toString() + ".header").exists());
    }
}
