/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.message;

import java.io.IOException;
import java.util.Map.Entry;

import javax.jms.JMSException;

import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;

import com.consol.citrus.adapter.handler.JmsConnectingMessageHandler.JmsMessageCallback;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.SoapAttachmentHeaders;

/**
 * Message callback able to decorate the generated JMS message before sending.
 * The callback will try to set SOAP attachment specific JMS header values if available.
 * 
 * @author Christoph Deppisch
 */
public class SoapAttachmentAwareJmsMessageCallback implements JmsMessageCallback {

    /**
     * @see com.consol.citrus.adapter.handler.JmsConnectingMessageHandler.JmsMessageCallback#doWithMessage(javax.jms.Message, org.springframework.integration.core.Message)
     */
    public void doWithMessage(javax.jms.Message jmsMessage, Message<?> request) throws JMSException {
        try {
            //explicitly take care of soap attachments in message header
            for (Entry<String, Object> headerEntry : request.getHeaders().entrySet()) {
                if(headerEntry.getValue() instanceof Attachment) {
                    Attachment attachment = (Attachment)headerEntry.getValue();
                    
                    String contentId = attachment.getContentId();
                    
                    //remove automatically added prefix and suffix (implementation specific: Saaj, Axiom)
                    if(contentId.startsWith("<")) {contentId = contentId.substring(1);}
                    if(contentId.endsWith(">")) {contentId = contentId.substring(0, contentId.length()-1);}
                    
                    jmsMessage.setStringProperty(SoapAttachmentHeaders.CONTENT_ID, contentId);
                    jmsMessage.setStringProperty(SoapAttachmentHeaders.CONTENT_TYPE, attachment.getContentType());
                    jmsMessage.setStringProperty(SoapAttachmentHeaders.CONTENT, FileUtils.readToString(attachment.getInputStream()).trim());
                    jmsMessage.setStringProperty(SoapAttachmentHeaders.CHARSET_NAME, "UTF-8");
                }
            }
        } catch(IOException e) {
            throw new CitrusRuntimeException("Unable to read SOAP attachment content", e);
        }
    }
}
