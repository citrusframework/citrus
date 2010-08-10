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

package com.consol.citrus.ws.validation;

import java.io.IOException;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.Test;

import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * @author Christoph Deppisch
 */
public class SimpleSoapAttachmentValidatorTest {
    
    @Test
    public void testSimpleValidation() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT, "This is a test!")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
    
    @Test
    public void testSimpleValidationUnknownContentId() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT, "This is a test!")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSimpleValidationWrongContentId() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT, "This is a test!")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("wrongAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is a test!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSimpleValidationWrongContent() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT, "This is a test!")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/plain");
        controlAttachment.setContent("This is not OK!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSimpleValidationWrongContentType() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT, "This is a test!")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/xml");
        controlAttachment.setContent("This is a test!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
}
