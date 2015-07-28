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
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Special SOAP receive message action builder adds SOAP specific properties.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class ReceiveSoapMessageBuilder extends ReceiveMessageBuilder<ReceiveSoapMessageAction, ReceiveSoapMessageBuilder> {

    /**
     * Default constructor using action and application context.
     * @param action
     */
    public ReceiveSoapMessageBuilder(ReceiveSoapMessageAction action) {
        super(action);
    }
    
    /**
     * Sets the control attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public ReceiveSoapMessageBuilder attachment(String contentId, String contentType, String content) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        attachment.setContent(content);

        action.getAttachments().add(attachment);

        return this;
    }
    
    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public ReceiveSoapMessageBuilder attachment(String contentId, String contentType, Resource contentResource) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);

        try {
            attachment.setContent(FileUtils.readToString(contentResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment content resource", e);
        }

        action.getAttachments().add(attachment);
        
        return this;
    }
    
    /**
     * Sets the charset name for this send action builder's control attachment.
     * @param charsetName
     * @return
     */
    public ReceiveSoapMessageBuilder charset(String charsetName) {
        if (!action.getAttachments().isEmpty()) {
            action.getAttachments().get(action.getAttachments().size() - 1).setCharsetName(charsetName);
        }
        return this;
    }
    
    /**
     * Sets the control attachment from Java object instance.
     * @param attachment
     * @return
     */
    public ReceiveSoapMessageBuilder attachment(SoapAttachment attachment) {
        action.getAttachments().add(attachment);
        return this;
    }

    /**
     * Set explicit SOAP attachment validator.
     * @param validator
     * @return
     */
    public ReceiveSoapMessageBuilder attachmentValidator(SoapAttachmentValidator validator) {
        action.setAttachmentValidator(validator);
        return this;
    }
    
    @Override
    public ReceiveSoapMessageBuilder soap() {
        return this;
    }

    @Override
    public ReceiveHttpMessageBuilder http() {
        throw new CitrusRuntimeException("Invalid use of http and soap action builder");
    }

}
