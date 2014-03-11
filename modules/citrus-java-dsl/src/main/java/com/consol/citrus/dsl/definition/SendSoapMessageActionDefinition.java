/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import org.springframework.core.io.Resource;
import org.springframework.integration.Message;
import org.springframework.oxm.Marshaller;

import java.io.IOException;

/**
 * Send action definition adding SOAP specific properties like SOAP attachment and
 * fork mode.
 * 
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionDefinition extends SendMessageActionDefinition {

    /**
     * Default constructor using action.
     * @param action
     */
    public SendSoapMessageActionDefinition(SendSoapMessageAction action) {
        super(action, null);
    }
    
    /**
     * Sets the attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SendSoapMessageActionDefinition attachment(String contentId, String contentType, String content) {
        getAction().setContentId(contentId);
        getAction().setContentType(contentType);
        getAction().setAttachmentData(content);
        
        return this;
    }
    
    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public SendSoapMessageActionDefinition attachment(String contentId, String contentType, Resource contentResource) {
        getAction().setContentId(contentId);
        getAction().setContentType(contentType);
        
        try {
            getAction().setAttachmentData(FileUtils.readToString(contentResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment resource", e);
        }
        
        return this;
    }
    
    /**
     * Sets the charset name for this send action definition's attachment.
     * @param charsetName
     * @return
     */
    public SendSoapMessageActionDefinition charset(String charsetName) {
        getAction().setCharsetName(charsetName);
        return this;
    }
    
    /**
     * Sets the attachment from Java object instance.
     * @param attachment
     * @return
     */
    public SendSoapMessageActionDefinition attachment(SoapAttachment attachment) {
        getAction().setContentId(attachment.getContentId());
        getAction().setContentType(attachment.getContentType());
        getAction().setAttachmentData(attachment.getContent());
        
        getAction().setCharsetName(attachment.getCharsetName());
        
        return this;
    }

    @Override
    public SendSoapMessageActionDefinition fork(boolean forkMode) {
        return (SendSoapMessageActionDefinition) super.fork(forkMode);
    }
    
    @Override
    public SendSoapMessageActionDefinition message(Message<String> message) {
        return (SendSoapMessageActionDefinition) super.message(message);
    }
    
    @Override
    public SendSoapMessageActionDefinition payload(Object payload, Marshaller marshaller) {
        return (SendSoapMessageActionDefinition) super.payload(payload, marshaller);
    }
    
    @Override
    public SendSoapMessageActionDefinition payload(Resource payloadResource) {
        return (SendSoapMessageActionDefinition) super.payload(payloadResource);
    }
    
    @Override
    public SendSoapMessageActionDefinition payload(String payload) {
        return (SendSoapMessageActionDefinition) super.payload(payload);
    }
    
    @Override
    public SendSoapMessageActionDefinition header(String name, Object value) {
        return (SendSoapMessageActionDefinition) super.header(name, value);
    }
    
    @Override
    public SendSoapMessageActionDefinition description(String description) {
        return (SendSoapMessageActionDefinition) super.description(description);
    }
    
    @Override
    public SendSoapMessageActionDefinition extractFromHeader(String headerName, String variable) {
        return (SendSoapMessageActionDefinition) super.extractFromHeader(headerName, variable);
    }
    
    @Override
    public SendSoapMessageActionDefinition extractFromPayload(String xpath, String variable) {
        return (SendSoapMessageActionDefinition) super.extractFromPayload(xpath, variable);
    }
    
    @Override
    public SendSoapMessageActionDefinition soap() {
        return this;
    }
    
    @Override
    public SendSoapMessageAction getAction() {
        return (SendSoapMessageAction)super.getAction();
    }

    @Override
    public SendHttpMessageActionDefinition http() {
        throw new CitrusRuntimeException("Invalid use of http and soap action definition");
    }
}
