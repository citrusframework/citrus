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

package com.consol.citrus.ws.actions;

import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Message send action able to add SOAP attachment support to normal message sending action.
 *  
 * @author Christoph Deppisch
 */
public class SendSoapMessageAction extends SendMessageAction {

    /** SOAP attachment */
    private List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();

    @Override
    protected SoapMessage createMessage(TestContext context, String messageType) {
        Message message = super.createMessage(context, getMessageType());

        final SoapMessage soapMessage = new SoapMessage(message);

        try {
            for (SoapAttachment attachment : attachments) {
                if (StringUtils.hasText(attachment.getContent())) {
                    attachment.setContent(context.replaceDynamicContentInString(attachment.getContent()));
                } else if (attachment.getContentResourcePath() != null) {
                    attachment.setContent(context.replaceDynamicContentInString(FileUtils.readToString(FileUtils.getFileResource(attachment.getContentResourcePath(), context))));
                }

                soapMessage.addAttachment(attachment);
            }

        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        return soapMessage;
    }

    /**
     * Gets the attachments.
     * @return the attachments
     */
    public List<SoapAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the control attachments.
     * @param attachments the control attachments
     */
    public void setAttachments(List<SoapAttachment> attachments) {
        this.attachments = attachments;
    }
}
