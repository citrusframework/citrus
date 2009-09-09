/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.ws.message;

import java.io.*;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.xml.namespace.QNameUtils;

import com.consol.citrus.message.MessageSender;
import com.consol.citrus.message.ReplyMessageHandler;

public class WebServiceMessageSender extends WebServiceGatewaySupport implements MessageSender {

    private ReplyMessageHandler replyMessageHandler;
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WebServiceMessageSender.class);
    
    public void send(final Message<?> message) {
        Assert.notNull(message, "Can not send empty message");
        
        log.info("Sending message to: " + getDefaultUri());

        if (log.isDebugEnabled()) {
            log.debug("Message to be sent:");
            log.debug(message.toString());
        }
        
        StringWriter responseWriter = new StringWriter();
        StreamResult result = new StreamResult(responseWriter);

        StreamSource source = new StreamSource(new StringReader(message.getPayload().toString()));
        getWebServiceTemplate().sendSourceAndReceiveToResult(source, 
                new WebServiceMessageCallback() {
                    public void doWithMessage(WebServiceMessage requestMessage)
                            throws IOException, TransformerException {
                        SoapMessage soapRequest = ((SoapMessage)requestMessage);
                        
                        for (Entry<String, Object> headerEntry : message.getHeaders().entrySet()) {
                            if(headerEntry.getKey().startsWith("springintegration")) {
                                continue;
                            }
                            
                            if(headerEntry.getKey().toLowerCase().endsWith("soapaction")) {
                                soapRequest.setSoapAction(headerEntry.getValue().toString());
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
                    }
        }, result);

        responseWriter.flush();
        if(replyMessageHandler != null) {
            replyMessageHandler.onReplyMessage(MessageBuilder.withPayload(responseWriter.toString()).build());
        }
        try {
            responseWriter.close();
        } catch (IOException e) {
            log.error("Error while closing output stream", e);
        }
    }

    /**
     * @param replyMessageHandler the replyMessageHandler to set
     */
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        this.replyMessageHandler = replyMessageHandler;
    }

}
