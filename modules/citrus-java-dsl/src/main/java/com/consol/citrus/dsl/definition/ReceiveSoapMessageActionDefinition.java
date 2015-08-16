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
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Special SOAP receive message action definition adds SOAP specific properties.
 * 
 * @author Christoph Deppisch
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.ReceiveSoapMessageBuilder}
 */
public class ReceiveSoapMessageActionDefinition extends ReceiveMessageActionDefinition<ReceiveSoapMessageAction, ReceiveSoapMessageActionDefinition> {

    /**
     * Default constructor using action and application context.
     * @param action
     */
    public ReceiveSoapMessageActionDefinition(ReceiveSoapMessageAction action) {
        super(action);
    }

    /**
     * Sets the control attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public ReceiveSoapMessageActionDefinition attachment(String contentId, String contentType, String content) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);
        attachment.setContent(content);

        getAction().getAttachments().add(attachment);

        return this;
    }

    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public ReceiveSoapMessageActionDefinition attachment(String contentId, String contentType, Resource contentResource) {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId(contentId);
        attachment.setContentType(contentType);

        try {
            attachment.setContent(FileUtils.readToString(contentResource));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read attachment content resource", e);
        }

        getAction().getAttachments().add(attachment);

        return this;
    }

    /**
     * Sets the charset name for this send action definition's control attachment.
     * @param charsetName
     * @return
     */
    public ReceiveSoapMessageActionDefinition charset(String charsetName) {
        if (!getAction().getAttachments().isEmpty()) {
            getAction().getAttachments().get(getAction().getAttachments().size() - 1).setCharsetName(charsetName);
        }
        return this;
    }

    /**
     * Sets the control attachment from Java object instance.
     * @param attachment
     * @return
     */
    public ReceiveSoapMessageActionDefinition attachment(SoapAttachment attachment) {
        getAction().getAttachments().add(attachment);
        return this;
    }

    /**
     * Set explicit SOAP attachment validator.
     * @param validator
     * @return
     */
    public ReceiveSoapMessageActionDefinition attachmentValidator(SoapAttachmentValidator validator) {
        getAction().setAttachmentValidator(validator);

        return this;
    }

    @Override
    public ReceiveSoapMessageActionDefinition soap() {
        return this;
    }

    @Override
    public ReceiveHttpMessageActionDefinition http() {
        throw new CitrusRuntimeException("Invalid use of http and soap action definition");
    }

}
