package com.consol.citrus.ws;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MessageEndpoint;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.namespace.QNameUtils;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHandler;
import com.consol.citrus.adapter.handler.EmptyResponseProducingMessageHandler;

public class WebServiceEndpoint implements MessageEndpoint {

    /** MessageHandler handling incoming requests and providing proper responses */
    private MessageHandler messageHandler = new EmptyResponseProducingMessageHandler();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceEndpoint.class);

    /**
     * @see org.springframework.ws.server.endpoint.MessageEndpoint#invoke(org.springframework.ws.context.MessageContext)
     * @throws CitrusRuntimeException
     */
    public void invoke(final MessageContext messageContext) throws Exception {
        
        WebServiceMessage request = messageContext.getRequest();
        Assert.notNull(request, "WebService request must not be null.");
        
        MessageBuilder<?> requestMessageBuilder = MessageBuilder.withPayload(request.getPayloadSource());
        
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
                Iterator<?> iter = soapHeader.getAllAttributes();
                while (iter.hasNext()) {
                    QName name = (QName) iter.next();
                    requestMessageBuilder.setHeader(name.toString(), soapHeader.getAttributeValue(name));
                }
            }
        }
        
        Message requestMessage = requestMessageBuilder.build();
        
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
            } else if (replyPayload instanceof String) {
                responseSource = new StringSource((String) replyPayload);
            } else {
                throw new CitrusRuntimeException("Unknown type for reply message payload (" + replyPayload.getClass().getName() + ") " +
                		"Supported types are " + 
                        "'" + Source.class.getName() + "', " +
                        "'" + Document.class.getName() + "'" + 
                        ", or 'java.lang.String'");
            }
            
            SoapMessage response = (SoapMessage)messageContext.getResponse();
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(responseSource, response.getPayloadResult());
            
            for (Entry<String, Object> headerEntry : replyMessage.getHeaders().entrySet()) {
                
                if(headerEntry.getKey().startsWith("springintegration")) {
                    continue;
                }
                
                if(headerEntry.getKey().toLowerCase().endsWith("soapaction")) {
                    response.setSoapAction(headerEntry.getValue().toString());
                } else {
                    SoapHeaderElement headerElement;
                    if(QNameUtils.validateQName(headerEntry.getKey())) {
                        headerElement = response.getSoapHeader().addHeaderElement(QNameUtils.parseQNameString(headerEntry.getKey()));
                    } else {
                        headerElement = response.getSoapHeader().addHeaderElement(QNameUtils.createQName("", headerEntry.getKey(), ""));
                    }
                    
                    headerElement.setText(headerEntry.getValue().toString());
                }
                
            }
        } else {
            log.error("Did not receive any reply from message handler '" + messageHandler + "'");
        }
    }

    /**
     * @param messageHandler the messageHandler to set
     */
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}