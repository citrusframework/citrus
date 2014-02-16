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

import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.util.MessageUtils;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;
import com.consol.citrus.ws.message.converter.SoapMessageConverter;
import com.consol.citrus.ws.util.SoapFaultDefinitionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.util.*;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.soap11.Soap11Body;
import org.springframework.ws.soap.soap12.Soap12Body;
import org.springframework.ws.soap.soap12.Soap12Fault;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * SpringWS {@link MessageEndpoint} implementation. Endpoint will delegate message processing to 
 * a {@link MessageHandler} implementation.
 * 
 * @author Christoph Deppisch
 */
public class WebServiceEndpoint implements MessageEndpoint {

    /** MessageHandler handling incoming requests and providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseEndpointAdapter();
    
    /** Default namespace for all SOAP header entries */
    private String defaultNamespaceUri;
    
    /** Default prefix for all SOAP header entries */
    private String defaultPrefix = "";
    
    /** Include mime headers (HTTP headers) into request which is passed to the message handler */
    private boolean handleMimeHeaders = false;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(WebServiceEndpoint.class);
    
    /** JMS headers begin with this prefix */
    private static final String DEFAULT_JMS_HEADER_PREFIX = "JMS";

    /**
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     * @throws CitrusRuntimeException
     */
    public void invoke(final MessageContext messageContext) throws Exception {
        Assert.notNull(messageContext.getRequest(), "Request must not be null - unable to send message");
        
        //build request message for message handler
        SoapMessageConverter messageConverter = new SoapMessageConverter(handleMimeHeaders);
        Message<?> requestMessage = messageConverter.convert(messageContext.getRequest(), messageContext);
        
        log.info("Received SOAP request:\n" + requestMessage.toString());
        
        //delegate request processing to message handler
        Message<?> replyMessage = messageHandler.handleMessage(requestMessage);
        
        if (simulateHttpStatusCode(replyMessage)) {
            return;
        }
        
        if (replyMessage != null && replyMessage.getPayload() != null) {
            log.info("Sending SOAP response:\n" + replyMessage.toString());
            
            SoapMessage response = (SoapMessage)messageContext.getResponse();
            
            //add soap fault or normal soap body to response
            if (replyMessage.getHeaders().containsKey(CitrusSoapMessageHeaders.SOAP_FAULT)) {
                addSoapFault(response, replyMessage);
            } else {
                addSoapBody(response, replyMessage);
            }
            
            addSoapHeaders(response, replyMessage);
            addMimeHeaders(response, replyMessage);
        } else {
            log.info("No reply message from message handler '" + messageHandler + "'");
            log.warn("No SOAP response for calling client");
        }
    }
    
    /**
     * If Http status code is set on reply message headers simulate Http error with status code.
     * No SOAP response is sent back in this case.
     * @param replyMessage
     * @return
     * @throws IOException 
     */
    private boolean simulateHttpStatusCode(Message<?> replyMessage) throws IOException {
        if (replyMessage == null || CollectionUtils.isEmpty(replyMessage.getHeaders())) {
            return false;
        }
        
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (headerEntry.getKey().toLowerCase().equals(CitrusSoapMessageHeaders.HTTP_STATUS_CODE)) {
                WebServiceConnection connection = TransportContextHolder.getTransportContext().getConnection();
                
                int statusCode = Integer.valueOf(headerEntry.getValue().toString());
                if (connection instanceof HttpServletConnection) {
                    ((HttpServletConnection)connection).setFault(false);
                    ((HttpServletConnection)connection).getHttpServletResponse().setStatus(statusCode);
                    return true;
                } else {
                    log.warn("Unable to set custom Http status code on connection other than HttpServletConnection (" + connection.getClass().getName() + ")");
                }
            }
        }
        
        return false;
    }

    /**
     * Adds mime headers outside of SOAP envelope. Header entries that go to this header section 
     * must have internal http header prefix defined in {@link CitrusSoapMessageHeaders}.
     * @param response the soap response message.
     * @param replyMessage the internal reply message.
     */
    private void addMimeHeaders(SoapMessage response, Message<?> replyMessage) {
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (headerEntry.getKey().toLowerCase().startsWith(CitrusSoapMessageHeaders.HTTP_PREFIX)) {
                String headerName = headerEntry.getKey().substring(CitrusSoapMessageHeaders.HTTP_PREFIX.length());
                
                if (response instanceof SaajSoapMessage) {
                    SaajSoapMessage saajSoapMessage = (SaajSoapMessage) response;
                    MimeHeaders headers = saajSoapMessage.getSaajMessage().getMimeHeaders();
                    headers.setHeader(headerName, headerEntry.getValue().toString());
                } else if (response instanceof AxiomSoapMessage) {
                    log.warn("Unable to set mime message header '" + headerName + "' on AxiomSoapMessage - unsupported");
                } else {
                    log.warn("Unsupported SOAP message implementation - unable to set mime message header '" + headerName + "'");
                }
            }
        }
    }

    /**
     * Add message payload as SOAP body element to the SOAP response.
     * @param response
     * @param replyMessage
     */
    private void addSoapBody(SoapMessage response, Message<?> replyMessage) throws TransformerException {
        if (!(replyMessage.getPayload() instanceof String) || 
                StringUtils.hasText(replyMessage.getPayload().toString())) {
            Source responseSource = getPayloadAsSource(replyMessage.getPayload());
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            transformer.transform(responseSource, response.getPayloadResult());
        }
    }
    
    /**
     * Translates message headers to SOAP headers in response.
     * @param response
     * @param replyMessage
     */
    private void addSoapHeaders(SoapMessage response, Message<?> replyMessage) throws TransformerException {
        for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
            if (MessageUtils.isSpringInternalHeader(headerEntry.getKey()) || 
                    headerEntry.getKey().startsWith(DEFAULT_JMS_HEADER_PREFIX)) {
                continue;
            }
            
            if (headerEntry.getKey().equalsIgnoreCase(CitrusSoapMessageHeaders.SOAP_ACTION)) {
                response.setSoapAction(headerEntry.getValue().toString());
            } else if (headerEntry.getKey().equalsIgnoreCase(CitrusMessageHeaders.HEADER_CONTENT)) {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                
                transformer.transform(new StringSource(headerEntry.getValue().toString()), 
                        response.getSoapHeader().getResult());
            } else if (headerEntry.getKey().startsWith(CitrusMessageHeaders.PREFIX)) {
                continue; //leave out Citrus internal header entries
            } else {
                SoapHeaderElement headerElement;
                if (QNameUtils.validateQName(headerEntry.getKey())) {
                    QName qname = QNameUtils.parseQNameString(headerEntry.getKey());
                    
                    if (StringUtils.hasText(qname.getNamespaceURI())) {
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
     * Adds a SOAP fault to the SOAP response body. The SOAP fault is declared
     * as QName string in the response message's header (see {@link CitrusSoapMessageHeaders})
     * 
     * @param response
     * @param replyMessage
     */
    private void addSoapFault(SoapMessage response, Message<?> replyMessage) throws TransformerException {
        SoapFaultDefinitionHolder definitionHolder = SoapFaultDefinitionHolder.fromString(
                replyMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_FAULT).toString());
        
        SoapFaultDefinition definition = definitionHolder.getSoapFaultDefinition();
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
        
        if (definitionHolder.getFaultActor() != null) {
            soapFault.setFaultActorOrRole(definitionHolder.getFaultActor());
        }
        
        List<String> soapFaultDetails = new ArrayList<String>();
        // add fault details
        for (Entry<String, Object> header : replyMessage.getHeaders().entrySet()) {
            if (header.getKey().startsWith(CitrusSoapMessageHeaders.SOAP_FAULT_DETAIL)) {
                soapFaultDetails.add(header.getValue().toString());
            }
        }
        
        if (!soapFaultDetails.isEmpty()) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            
            SoapFaultDetail faultDetail = soapFault.addFaultDetail();;
            for (int i = 0; i < soapFaultDetails.size(); i++) {
                transformer.transform(new StringSource(soapFaultDetails.get(i)), faultDetail.getResult());
            }
        }
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
        } else if (replyPayload instanceof String) {
            return new StringSource((String) replyPayload);
        } else {
            throw new CitrusRuntimeException("Unknown type for reply message payload (" + replyPayload.getClass().getName() + ") " +
                    "Supported types are " + "'" + Source.class.getName() + "', " + "'" + Document.class.getName() + "'" + 
                    ", or '" + String.class.getName() + "'");
        }
    }

    /**
     * Get the default QName from local part.
     * @param localPart
     * @return
     */
    private QName getDefaultQName(String localPart) {
        if (StringUtils.hasText(defaultNamespaceUri)) {
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

    /**
     * Enable mime headers in request message which is passed to message handler.
     * @param handleMimeHeaders the handleMimeHeaders to set
     */
    public void setHandleMimeHeaders(boolean handleMimeHeaders) {
        this.handleMimeHeaders = handleMimeHeaders;
    }
}