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

import java.net.URI;

import org.springframework.core.JdkVersion;
import org.springframework.integration.Message;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.*;
import org.springframework.ws.soap.addressing.version.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.addressing.WsAddressingVersion;

/**
 * Sender callback invoked by framework with actual web service request before message is sent.
 * Web service message is filled with content from internal message representation.
 * 
 * @author Christoph Deppisch
 */
public class WsAddressingRequestMessageCallback extends SoapRequestMessageCallback {
    
    /** Ws addressing headers */
    private WsAddressingHeaders addressingHeaders;
    
    /**
     * Default constructor using fields.
     * @param message
     * @param attachment
     */
    public WsAddressingRequestMessageCallback(Message<?> message, Attachment attachment, 
            WsAddressingHeaders addressingHeaders) {
        super(message, attachment);
        this.addressingHeaders = addressingHeaders;
    }

    /**
     * Update message with ws addressing header information.
     */
    public void doWithSoapRequest(SoapMessage soapMessage) {
        URI messageId;
        
        if (addressingHeaders.getMessageId() != null) {
            messageId = addressingHeaders.getMessageId();
        } else {
            messageId = getMessageIdStrategy().newMessageId(soapMessage);
        }
            
        MessageAddressingProperties map =
                new MessageAddressingProperties(addressingHeaders.getTo(), 
                        addressingHeaders.getFrom(), 
                        addressingHeaders.getReplyTo(), 
                        addressingHeaders.getFaultTo(), 
                        addressingHeaders.getAction(), 
                        messageId);
        
        AddressingVersion version;
        if (addressingHeaders.getVersion().equals(WsAddressingVersion.VERSION10)) {
            version = new Addressing10();
        } else if (addressingHeaders.getVersion().equals(WsAddressingVersion.VERSION200408)) {
            version = new Addressing200408();
        } else {
            throw new CitrusRuntimeException("Unsupported ws addressing version '" + 
                    addressingHeaders.getVersion() + "'");
        }
        
        version.addAddressingHeaders(soapMessage, map);
    }

    /**
     * Get the message id generation strategy.
     * @return
     */
    @SuppressWarnings("deprecation")
    private MessageIdStrategy getMessageIdStrategy() {
        if (JdkVersion.getMajorJavaVersion() > JdkVersion.JAVA_14) {
            return new UuidMessageIdStrategy();
        } else {
            return new RandomGuidMessageIdStrategy();
        }
    }
}
