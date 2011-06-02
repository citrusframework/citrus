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

package com.consol.citrus.ws.message.callback;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.xml.transform.StringResult;

import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * Receiver callback invoked by framework on response message. Callback fills an internal message representation with
 * the response information for further message processing.
 * 
 * @author Christoph Deppisch
 */
public class SoapResponseMessageCallback implements WebServiceMessageCallback {

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SoapResponseMessageCallback.class);
    
    /** The response message built from WebService response message */
    private Message<String> response;

    /**
     * Callback method called on response message.
     */
    public void doWithMessage(WebServiceMessage responseMessage) throws IOException, TransformerException {
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        StringResult responsePayload = new StringResult();
        transformer.transform(responseMessage.getPayloadSource(), responsePayload);
        
        MessageBuilder<String> responseMessageBuilder = MessageBuilder.withPayload(responsePayload.toString());
        
        if (responseMessage instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) responseMessage;
            SoapHeader soapHeader = soapMessage.getSoapHeader();
            
            if (soapHeader != null) {
                Iterator<?> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
                    responseMessageBuilder.setHeader(headerEntry.getName().getLocalPart(), headerEntry.getText());
                }
            }
            
            if(StringUtils.hasText(soapMessage.getSoapAction())) {
                if(soapMessage.getSoapAction().equals("\"\"")) {
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, "");
                } else {
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, soapMessage.getSoapAction());
                }
            }
            
            Iterator<?> attachments = soapMessage.getAttachments();

            while (attachments.hasNext()) {
                Attachment attachment = (Attachment)attachments.next();
                
                if(StringUtils.hasText(attachment.getContentId())) {
                    String contentId = attachment.getContentId();
                    
                    if(contentId.startsWith("<")) {contentId = contentId.substring(1);}
                    if(contentId.endsWith(">")) {contentId = contentId.substring(0, contentId.length()-1);}
                    
                    if(log.isDebugEnabled()) {
                        log.debug("Response contains attachment with contentId '" + contentId + "'");
                    }
                    
                    responseMessageBuilder.setHeader(contentId, attachment);                        
                    // TODO CW: is this ok here or do we have to include the SoapAttachmentAwareJmsCallback?
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_ID, contentId);
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT_TYPE, attachment.getContentType());
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CONTENT, FileUtils.readToString(attachment.getInputStream()).trim());
                    responseMessageBuilder.setHeader(CitrusSoapMessageHeaders.CHARSET_NAME, "UTF-8");
                } else {
                    log.warn("Could not handle response attachment with empty 'contentId'. Attachment is ignored in further processing");
                }
            }
        }
        
        // now set response for later access via getResponse():
        response = responseMessageBuilder.build();
    }
    
    public Message<String> getResponse() {
        return response;
    }
}
