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

package com.consol.citrus.ws.message;

import java.io.IOException;
import java.util.Map.Entry;

import javax.jms.JMSException;

import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;

import com.consol.citrus.adapter.handler.JmsConnectingMessageHandler.JmsMessageCallback;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.FileUtils;

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
                    
                    jmsMessage.setStringProperty(CitrusSoapMessageHeaders.CONTENT_ID, contentId);
                    jmsMessage.setStringProperty(CitrusSoapMessageHeaders.CONTENT_TYPE, attachment.getContentType());
                    jmsMessage.setStringProperty(CitrusSoapMessageHeaders.CONTENT, FileUtils.readToString(attachment.getInputStream()).trim());
                    jmsMessage.setStringProperty(CitrusSoapMessageHeaders.CHARSET_NAME, "UTF-8");
                }
            }
        } catch(IOException e) {
            throw new CitrusRuntimeException("Unable to read SOAP attachment content", e);
        }
    }
}
