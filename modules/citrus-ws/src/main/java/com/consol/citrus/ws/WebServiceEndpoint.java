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

package com.consol.citrus.ws;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinitionEditor;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;

/**
 * SpringWS {@link MessageEndpoint} implementation. Endpoint will delegate message processing to 
 * a {@link MessageHandler} implementation.
 * 
 * @author Christoph Deppisch
 */
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
    
    /** JMS headers begin with this prefix */
    private static final String DEFAULT_JMS_HEADER_PREFIX = "JMS";

    /**
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     * @throws CitrusRuntimeException
     */
    public void invoke(final MessageContext messageContext) throws Exception {
        Assert.notNull(messageContext.getRequest(), "WebService request must not be null.");
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        StringResult requestPayload = new StringResult();
        transformer.transform(messageContext.getRequest().getPayloadSource(), requestPayload);
        
        //build request message for message handler
        Message<?> requestMessage = buildRequestMessage(messageContext, requestPayload.toString());
        
        log.info("Received WebService request " + requestMessage);
        
        //delegate request processing to message handler
        Message<?> replyMessage = messageHandler.handleMessage(requestMessage);
        
        if (replyMessage != null && replyMessage.getPayload() != null) {
            log.info("Sending WebService response " + replyMessage);
            
            SoapMessage response = (SoapMessage)messageContext.getResponse();
            
            //add soap fault or normal soap body to response
            if(replyMessage.getHeaders().containsKey(CitrusSoapMessageHeaders.SOAP_FAULT)) {
                addSoapFault(response, replyMessage, transformer);
            } else {
                addSoapBody(response, replyMessage, transformer);
            }
            
            addSoapHeaders(response, replyMessage);
        } else {
            log.warn("Did not receive any reply from message handler '" + messageHandler + "'");
        }
    }

    /**
     * Add message payload as SOAP body element to the SOAP response.
     * @param response
     * @param replyMessage
     * @param transformer
     */
    private void addSoapBody(SoapMessage response, Message<?> replyMessage, Transformer transformer) throws TransformerException {
        Source responseSource = getPayloadAsSource(replyMessage.getPayload());
        
        transformer.transform(responseSource, response.getPayloadResult());
    }

    /**
     * Get the message payload object as {@link Source}, supported payload types are
     * {@link Source}, {@link Document} and {@link String}.
     * @param replyPayload payload object
     * @return {@link Source} representation of the payload
     */
    private Source getPayloadAsSource(Object replyPayload) {
        if (replyPayload instanceof Source) {
            return (Source) replyPayload;
        } else if (replyPayload instanceof Document) {
            return new DOMSource((Document) replyPayload);
        } else if (replyPayload instanceof String && StringUtils.hasText(replyPayload.toString())) {
            return new StringSource((String) replyPayload);
        } else {
            throw new CitrusRuntimeException("Unknown type for reply message payload (" + replyPayload.getClass().getName() + ") " +
                    "Supported types are " + 
                    "'" + Source.class.getName() + "', " +
                    "'" + Document.class.getName() + "'" + 
                    ", or 'java.lang.String'");
        }
    }

    /**
     * Translates message headers to SOAP headers in response.
     * @param response
     * @param replyMessage
     */
    private void addSoapHeaders(SoapMessage response, Message<?> replyMessage) throws TransformerException {
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if(MessageUtils.isSpringInternalHeader(headerEntry.getKey()) || 
                    headerEntry.getKey().startsWith(DEFAULT_JMS_HEADER_PREFIX)) {
                continue;
            }
            
            if(headerEntry.getKey().toLowerCase().equals(CitrusSoapMessageHeaders.SOAP_ACTION)) {
                response.setSoapAction(headerEntry.getValue().toString());
            } else if(headerEntry.getKey().toLowerCase().equals(CitrusMessageHeaders.HEADER_CONTENT)) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                
                transformer.transform(new StringSource(headerEntry.getValue().toString()), 
                        response.getSoapHeader().getResult());
            } else if(headerEntry.getKey().startsWith(CitrusMessageHeaders.PREFIX)) {
                continue; //leave out Citrus internal header entries
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
    }

    /**
     * Transform incoming {@link WebServiceMessage} into a proper {@link Message} instance.
     * Specific SOAP message parts are translated to message headers with special names (e.g. SOAP attachments).
     * See {@link CitrusSoapMessageHeaders} for details.
     * 
     * @param messageContext
     * @param requestPayload
     * @return the request message with message headers set.
     */
    private Message<?> buildRequestMessage(MessageContext messageContext, String requestPayload) {
        WebServiceMessage request = messageContext.getRequest();
        
        MessageBuilder<?> requestMessageBuilder = MessageBuilder.withPayload(requestPayload);
        
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
            
            if(StringUtils.hasText(soapMessage.getSoapAction())) {
                if(soapMessage.getSoapAction().equals("\"\"")) {
                    requestMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, "");
                } else {
                    requestMessageBuilder.setHeader(CitrusSoapMessageHeaders.SOAP_ACTION, soapMessage.getSoapAction());
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
        
        return requestMessageBuilder.build();
    }

    /**
     * Adds a SOAP fault to the SOAP response body. The SOAP fault is declared
     * as QName string in the response message's header (see {@link CitrusSoapMessageHeaders})
     * 
     * @param response
     * @param soapFaultString
     */
    private void addSoapFault(SoapMessage response, Message<?> replyMessage, Transformer transformer) throws TransformerException {
        SoapFaultDefinitionEditor definitionEditor = new SoapFaultDefinitionEditor();
        definitionEditor.setAsText(replyMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT).toString());
        
        SoapFaultDefinition definition = (SoapFaultDefinition)definitionEditor.getValue();
        SoapBody soapBody = response.getSoapBody();
        SoapFault soapFault = null;
        
        if (SoapFaultDefinition.SERVER.equals(definition.getFaultCode()) ||
                SoapFaultDefinition.RECEIVER.equals(definition.getFaultCode())) {
            soapFault = soapBody.addServerOrReceiverFault(definition.getFaultStringOrReason(), 
                    definition.getLocale());
        } else if (SoapFaultDefinition.CLIENT.equals(definition.getFaultCode()) ||
                SoapFaultDefinition.SENDER.equals(definition.getFaultCode())) {
            soapFault = soapBody.addClientOrSenderFault(definition.getFaultStringOrReason(), 
                    definition.getLocale());
        } else if (soapBody instanceof Soap11Body) {
            Soap11Body soap11Body = (Soap11Body) soapBody;
            soapFault = soap11Body.addFault(definition.getFaultCode(), 
                    definition.getFaultStringOrReason(), 
                    definition.getLocale());
        } else if (soapBody instanceof Soap12Body) {
            Soap12Body soap12Body = (Soap12Body) soapBody;
            Soap12Fault soap12Fault =
                    (Soap12Fault) soap12Body.addServerOrReceiverFault(definition.getFaultStringOrReason(), 
                            definition.getLocale());
            soap12Fault.addFaultSubcode(definition.getFaultCode());
            
            soapFault = soap12Fault;
        } else {
                throw new CitrusRuntimeException("Found unsupported SOAP implementation. Use SOAP 1.1 or SOAP 1.2.");
        }
        
        if(replyMessage.getPayload() instanceof String && 
                StringUtils.hasText(replyMessage.getPayload().toString())) {
            SoapFaultDetail faultDetail = soapFault.addFaultDetail();
            transformer.transform(getPayloadAsSource(replyMessage.getPayload()), faultDetail.getResult());
        }
    }

    /**
     * Get the default QName from local part.
     * @param localPart
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
     * Set the message handler.
     * @param messageHandler the messageHandler to set
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Set the default namespace used in SOAP response headers.
     * @param defaultNamespaceUri the defaultNamespaceUri to set
     */
    public void setDefaultNamespaceUri(String defaultNamespaceUri) {
        this.defaultNamespaceUri = defaultNamespaceUri;
    }

    /**
     * Set the default namespace prefix used in SOAP response headers.
     * @param defaultPrefix the defaultPrefix to set
     */
    public void setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix;
    }
}