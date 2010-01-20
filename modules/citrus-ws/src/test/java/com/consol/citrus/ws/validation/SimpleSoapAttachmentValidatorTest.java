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

package com.consol.citrus.ws.validation;

import java.io.IOException;

import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.Test;

import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.SoapAttachmentHeaders;

/**
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 */
public class SimpleSoapAttachmentValidatorTest {
    
    @Test
    public void testSimpleValidation() throws IOException {
        Message<?> testMessage = MessageBuilder.withPayload("Some Payload")
                                    .setHeader(SoapAttachmentHeaders.CONTENT, "This is a test!")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_TYPE, "text/plain")
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
                                    .setHeader(SoapAttachmentHeaders.CONTENT, "This is a test!")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_TYPE, "text/plain")
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
                                    .setHeader(SoapAttachmentHeaders.CONTENT, "This is a test!")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_TYPE, "text/plain")
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
                                    .setHeader(SoapAttachmentHeaders.CONTENT, "This is a test!")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_TYPE, "text/plain")
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
                                    .setHeader(SoapAttachmentHeaders.CONTENT, "This is a test!")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_ID, "soapAttachmentId")
                                    .setHeader(SoapAttachmentHeaders.CONTENT_TYPE, "text/plain")
                                    .build();
        
        SoapAttachment controlAttachment = new SoapAttachment();
        controlAttachment.setContentId("soapAttachmentId");
        controlAttachment.setContentType("text/xml");
        controlAttachment.setContent("This is a test!");
        
        SimpleSoapAttachmentValidator validator = new SimpleSoapAttachmentValidator();
        validator.validateAttachment(testMessage, controlAttachment);
    }
}
