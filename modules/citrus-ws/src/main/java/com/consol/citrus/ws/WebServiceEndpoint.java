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

package com.consol.citrus.ws;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.*;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;

public class WebServiceEndpoint implements MessageEndpoint {

    /** MessageHandler handling incoming requests and providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
    
    /** Default namespace for all SOAP header entries */
    private String defaultNamespaceUri;
    
    /** Default prefix for all SOAP header entries */
    private String defaultPrefix = "";
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceEndpoint.class);
    
    private static final String DEFAULT_JMS_HEADER_PREFIX = "JMS";

    /**
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     * @throws CitrusRuntimeException
     */
    public void invoke(final MessageContext messageContext) throws Exception {
        WebServiceMessage request = messageContext.getRequest();
        Assert.notNull(request, "WebService request must not be null.");

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        StringResult requestPayload = new StringResult();
        transformer.transform(request.getPayloadSource(), requestPayload);
        
        MessageBuilder<?> requestMessageBuilder = MessageBuilder.withPayload(requestPayload.toString());
        
        String[] propertyNames = messageContext.getPropertyNames();
        if (propertyNames != null) {
            for (String propertyName : propertyNames) {
                requestMessageBuilder.setHeader(propertyName, messageContext.getProperty(propertyName));
            }
        }
        
        if (request instanceof SoapMessage) {
            SoapMessage soapMessage = (SoapMessage) request;
            SoapHeader soapHeader = soapMessage.getSoapHeader();
            
            if (soapHeader != null) {
                Iterator<?> iter = soapHeader.examineAllHeaderElements();
                while (iter.hasNext()) {
                    SoapHeaderElement headerEntry = (SoapHeaderElement) iter.next();
                    requestMessageBuilder.setHeader(headerEntry.getName().getLocalPart(), headerEntry.getText());
                }
            }
            
            Iterator<?> attachments = soapMessage.getAttachments();
            while (attachments.hasNext()) {
                Attachment attachment = (Attachment)attachments.next();
                
                if(StringUtils.hasText(attachment.getContentId())) {
                    String contentId = attachment.getContentId();
                    
                    if(contentId.startsWith("<")) {contentId = contentId.substring(1);}
                    if(contentId.endsWith(">")) {contentId = contentId.substring(0, contentId.length()-1);}
                    
                    requestMessageBuilder.setHeader(contentId, attachment);
                } else {
                    log.warn("Could not handle attachment with empty 'contentId'. Attachment is ignored in further processing");
                }
            }
        }
        
        Message<?> requestMessage = requestMessageBuilder.build();
        
        log.info("Received WebService request " + requestMessage);
        
        Message<?> replyMessage = messageHandler.handleMessage(requestMessage);
        
        if (replyMessage != null && replyMessage.getPayload() != null) {
            log.info("Sending WebService response " + replyMessage);
            
            Object replyPayload = replyMessage.getPayload();
            Source responseSource = null;
            
            if (replyPayload instanceof Source) {
                responseSource = (Source) replyPayload;
            } else if (replyPayload instanceof Document) {
                responseSource = new DOMSource((Document) replyPayload);
            } else if (replyPayload instanceof String && StringUtils.hasText(replyPayload.toString())) {
                responseSource = new StringSource((String) replyPayload);
            } else {
                throw new CitrusRuntimeException("Unknown type for reply message payload (" + replyPayload.getClass().getName() + ") " +
                		"Supported types are " + 
                        "'" + Source.class.getName() + "', " +
                        "'" + Document.class.getName() + "'" + 
                        ", or 'java.lang.String'");
            }
            
            SoapMessage response = (SoapMessage)messageContext.getResponse();
            
            if(response != null) {
                transformer.transform(responseSource, response.getPayloadResult());
            }
            
            for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
                
                if(headerEntry.getKey().startsWith(MessageHeaders.PREFIX) || 
                        headerEntry.getKey().startsWith(DEFAULT_JMS_HEADER_PREFIX)) {
                    continue;
                }
                
                if(headerEntry.getKey().toLowerCase().endsWith("soapaction")) {
                    response.setSoapAction(headerEntry.getValue().toString());
                } else {
                    SoapHeaderElement headerElement;
                    if(QNameUtils.validateQName(headerEntry.getKey())) {
                        QName qname = QNameUtils.parseQNameString(headerEntry.getKey());
                        
                        if(StringUtils.hasText(qname.getNamespaceURI())) {
                            headerElement = response.getSoapHeader().addHeaderElement(qname);
                        } else {
                            headerElement = response.getSoapHeader().addHeaderElement(getDefaultQName(headerEntry.getKey()));
                        }
                    } else {
                        throw new SoapHeaderException("Failed to add SOAP header '" + headerEntry.getKey() + "', " +
                        		"because of invalid QName");
                    }
                    
                    headerElement.setText(headerEntry.getValue().toString());
                }
                
            }
        } else {
            log.warn("Did not receive any reply from message handler '" + messageHandler + "'");
        }
    }

    /**
     * @param key
     * @return
     */
    private QName getDefaultQName(String localPart) {
        if(StringUtils.hasText(defaultNamespaceUri)) {
            return QNameUtils.createQName(defaultNamespaceUri, localPart, defaultPrefix);
        } else {
            throw new SoapHeaderException("Failed to add SOAP header '" + localPart + "', " +
            		"because neither valid QName nor default namespace-uri is set!");
        }
    }

    /**
     * @param messageHandler the messageHandler to set
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * @param defaultNamespaceUri the defaultNamespaceUri to set
     */
    public void setDefaultNamespaceUri(String defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    /**
     * @param defaultPrefix the defaultPrefix to set
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }
}