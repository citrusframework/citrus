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

package org.citrusframework.ws.message.converter;

import java.net.URI;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.ws.addressing.WsAddressingHeaders;
import org.citrusframework.ws.addressing.WsAddressingMessageHeaders;
import org.citrusframework.ws.addressing.WsAddressingVersion;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.addressing.core.EndpointReference;
import org.springframework.ws.soap.addressing.core.MessageAddressingProperties;
import org.springframework.ws.soap.addressing.messageid.MessageIdStrategy;
import org.springframework.ws.soap.addressing.messageid.UuidMessageIdStrategy;
import org.springframework.ws.soap.addressing.version.Addressing10;
import org.springframework.ws.soap.addressing.version.Addressing200408;
import org.springframework.ws.soap.addressing.version.AddressingVersion;

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
    public void convertOutbound(WebServiceMessage webServiceMessage, Message message, WebServiceEndpointConfiguration endpointConfiguration, TestContext context) {
        super.convertOutbound(webServiceMessage, message, endpointConfiguration, context);

        SoapMessage soapMessage = (SoapMessage) webServiceMessage;
        URI messageId;
        if (message.getHeader(WsAddressingMessageHeaders.MESSAGE_ID) != null) {
            messageId = URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.MESSAGE_ID).toString()));
        } else if (addressingHeaders.getMessageId() != null) {
            messageId = addressingHeaders.getMessageId();
        } else {
            messageId = messageIdStrategy.newMessageId(soapMessage);
        }

        EndpointReference from = addressingHeaders.getFrom();
        if (message.getHeader(WsAddressingMessageHeaders.FROM) != null) {
            from = new EndpointReference(URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.FROM).toString())));
        }

        URI to = addressingHeaders.getTo();
        if (message.getHeader(WsAddressingMessageHeaders.TO) != null) {
            to = URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.TO).toString()));
        }

        URI action = addressingHeaders.getAction();
        if (message.getHeader(WsAddressingMessageHeaders.ACTION) != null) {
            action = URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.ACTION).toString()));
        }

        EndpointReference replyTo = addressingHeaders.getReplyTo();
        if (message.getHeader(WsAddressingMessageHeaders.REPLY_TO) != null) {
            replyTo = new EndpointReference(URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.REPLY_TO).toString())));
        }

        EndpointReference faultTo = addressingHeaders.getReplyTo();
        if (message.getHeader(WsAddressingMessageHeaders.FAULT_TO) != null) {
            faultTo = new EndpointReference(URI.create(context.replaceDynamicContentInString(message.getHeader(WsAddressingMessageHeaders.FAULT_TO).toString())));
        }

        MessageAddressingProperties map =
                new MessageAddressingProperties(to,
                        from,
                        replyTo,
                        faultTo,
                        action,
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

        if (addressingHeaders.hasMustUnderstandHeaders()) {
            soapMessage.getSoapHeader().examineAllHeaderElements().forEachRemaining(header -> {
                if (addressingHeaders.isMustUnderstand(header.getName())) {
                    header.setMustUnderstand(true);
                }
            });
        }
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
