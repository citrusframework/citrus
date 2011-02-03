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
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.soap.MimeHeaders;
import javax.xml.transform.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.integration.core.Message;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringSource;

import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * Sender callback invoked by framework with actual web service request before message is sent.
 * Web service message is filled with content from internal message representation.
 * 
 * @author Christoph Deppisch
 */
public class SoapRequestMessageCallback implements WebServiceMessageCallback {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SoapRequestMessageCallback.class);
    
    /** The internal message content source */
    private Message<?> message;
    
    /** Optional attachment */
    private Attachment attachment = null;
    
    /**
     * Default constructor using fields.
     * @param message
     * @param attachment
     */
    public SoapRequestMessageCallback(Message<?> message, Attachment attachment) {
        this.message = message;
        this.attachment = attachment;
    }

    /**
     * Callback method called before request message  is sent.
     */
    public void doWithMessage(WebServiceMessage requestMessage) throws IOException, TransformerException {
        SoapMessage soapRequest = ((SoapMessage)requestMessage);
        
        // Copy payload into soap-body: 
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new StringSource(message.getPayload().toString()), soapRequest.getSoapBody().getPayloadResult());
        
        // Copy headers into soap-header:
        for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
            if(MessageUtils.isSpringInternalHeader(headerEntry.getKey())) {
                continue;
            }
            
            if(headerEntry.getKey().toLowerCase().equals(CitrusSoapMessageHeaders.SOAP_ACTION)) {
                soapRequest.setSoapAction(headerEntry.getValue().toString());
            } else if(headerEntry.getKey().toLowerCase().equals(CitrusMessageHeaders.HEADER_CONTENT)) {
                transformer.transform(new StringSource(headerEntry.getValue().toString()), 
                        soapRequest.getSoapHeader().getResult());
            } else if (headerEntry.getKey().toLowerCase().startsWith(CitrusSoapMessageHeaders.HTTP_PREFIX)) {
                addMimeMessageHeader(soapRequest, 
                        headerEntry.getKey().substring(CitrusSoapMessageHeaders.HTTP_PREFIX.length()), 
                        headerEntry.getValue());
            } else {
                SoapHeaderElement headerElement;
                if(QNameUtils.validateQName(headerEntry.getKey())) {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
                } else {
                    headerElement = soapRequest.getSoapHeader().addHeaderElement(QNameUtils.createQName("", headerEntry.getKey(), ""));
                }
                
                headerElement.setText(headerEntry.getValue().toString());
            }
        }
        // Add attachment:
        if(attachment != null) {
            if(log.isDebugEnabled()) {
                log.debug("Adding attachment to SOAP message: '" + attachment.getContentId() + "' ('" + attachment.getContentType() + "')");
            }
            
            soapRequest.addAttachment(attachment.getContentId(), new InputStreamSource() {
                public InputStream getInputStream() throws IOException {
                    return attachment.getInputStream();
                }
            }, attachment.getContentType());
        }
        
        doWithSoapRequest(soapRequest);
    }

    /**
     * Adds a HTTP message header to the SOAP message.
     * 
     * @param message the SOAP request message.
     * @param name the header name.
     * @param value the header value.
     */
    private void addMimeMessageHeader(SoapMessage message, String name, Object value) {
        if (message instanceof SaajSoapMessage) {
            SaajSoapMessage soapMsg = (SaajSoapMessage) message;
            MimeHeaders headers = soapMsg.getSaajMessage().getMimeHeaders();
            headers.setHeader(name, value.toString());
        } else if (message instanceof AxiomSoapMessage) {
            log.warn("Unable to set mime message header '" + name + "' on AxiomSoapMessage - unsupported");
        } else {
            log.warn("Unsupported SOAP message implementation - unable to set mime message header '" + name + "'");
        }
    }

    /**
     * Subclasses may use this method in order to manipulate the Soap request before sending.
     * @param soapRequest the request message.
     */
    protected void doWithSoapRequest(SoapMessage soapRequest) {
    }
}
