/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.ws.message.converter;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.ws.addressing.WsAddressingHeaders;
import com.consol.citrus.ws.addressing.WsAddressingVersion;
import com.consol.citrus.ws.client.WebServiceEndpointConfiguration;
import com.consol.citrus.message.Message;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.*;

import java.net.URI;

/**
 * Ws addressing aware message converter implementation. Adds addressing header information to SOAP header.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public class WsAddressingMessageConverter extends SoapMessageConverter {

    /** Ws addressing headers */
    private final WsAddressingHeaders addressingHeaders;

    /** Message id generation strategy */
    private MessageIdStrategy messageIdStrategy = new UuidMessageIdStrategy();

    /**
     * Default constructor using addressing headers.
     * @param addressingHeaders
     */
    public WsAddressingMessageConverter(WsAddressingHeaders addressingHeaders) {
        this.addressingHeaders = addressingHeaders;
    }

    @Override
    public void convertOutbound(WebServiceMessage webServiceMessage, Message message, WebServiceEndpointConfiguration endpointConfiguration) {
        super.convertOutbound(webServiceMessage, message, endpointConfiguration);

        SoapMessage soapMessage = (SoapMessage) webServiceMessage;
        URI messageId;
        if (addressingHeaders.getMessageId() != null) {
            messageId = addressingHeaders.getMessageId();
        } else {
            messageId = messageIdStrategy.newMessageId(soapMessage);
        }

        MessageAddressingProperties map =
                new MessageAddressingProperties(addressingHeaders.getTo(),
                        addressingHeaders.getFrom(),
                        addressingHeaders.getReplyTo(),
                        addressingHeaders.getFaultTo(),
                        addressingHeaders.getAction(),
                        messageId);

        AddressingVersion version;
        // avoid NPE
        if (WsAddressingVersion.VERSION10.equals(addressingHeaders.getVersion())) {
            version = new Addressing10();
        } else if (WsAddressingVersion.VERSION200408.equals(addressingHeaders.getVersion())) {
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
    public MessageIdStrategy getMessageIdStrategy() {
        return messageIdStrategy;
    }

    /**
     * Sets the message id generation strategy.
     * @param messageIdStrategy
     */
    public void setMessageIdStrategy(MessageIdStrategy messageIdStrategy) {
        this.messageIdStrategy = messageIdStrategy;
    }

    /**
     * Gets the addressing headers.
     * @return
     */
    public WsAddressingHeaders getAddressingHeaders() {
        return addressingHeaders;
    }
}
