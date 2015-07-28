/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessageHeaders;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Send action builder adding SOAP specific properties like SOAP attachment and
 * fork mode.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SendSoapMessageBuilder extends SendMessageBuilder<SendSoapMessageAction, SendSoapMessageBuilder> {

    /**
     * Default constructor using action.
     * @param action
     */
    public SendSoapMessageBuilder(SendSoapMessageAction action) {
        super(action);
    }

    /**
     * Sets special SOAP action message header.
     * @param soapAction
     * @return
     */
    public SendSoapMessageBuilder soapAction(String soapAction) {
        header(SoapMessageHeaders.SOAP_ACTION, soapAction);
        return this;
    }
    
    /**
     * Sets the attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SendSoapMessageBuilder attachment(String contentId, String contentType, String content) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        attachment.setContent(content);

        action.getAttachments().add(attachment);
        return this;
    }
    
    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public SendSoapMessageBuilder attachment(String contentId, String contentType, Resource contentResource) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        
        try {
            attachment.setContent(FileUtils.readToString(contentResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment resource", e);
        }

        action.getAttachments().add(attachment);
        
        return this;
    }
    
    /**
     * Sets the charset name for this send action builder's attachment.
     * @param charsetName
     * @return
     */
    public SendSoapMessageBuilder charset(String charsetName) {
        if (!action.getAttachments().isEmpty()) {
            action.getAttachments().get(action.getAttachments().size() - 1).setCharsetName(charsetName);
        }
        return this;
    }
    
    /**
     * Sets the attachment from Java object instance.
     * @param attachment
     * @return
     */
    public SendSoapMessageBuilder attachment(SoapAttachment attachment) {
        action.getAttachments().add(attachment);
        return this;
    }

    @Override
    public SendSoapMessageBuilder soap() {
        return this;
    }

    @Override
    public SendHttpMessageBuilder http() {
        throw new CitrusRuntimeException("Invalid use of http and soap action builder");
    }
}
