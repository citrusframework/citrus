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

import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;

/**
 * Converter is abel to create proper WebService message from internal message representation and vice versa. Converter
 * is used both on client and server side to convert SOAP request and response messages to internal message representation.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface WebServiceMessageConverter extends MessageConverter<WebServiceMessage, WebServiceMessage, WebServiceEndpointConfiguration> {

    /**
     * Conversion method for inbound messages. Given inbound WebService message is translated to internal message representation. Given message context is
     * optional and if present provides access to incoming request information such as message properties.
     *
     * @param webServiceMessage the initial web service message.
     * @param messageContext optional message context.
     * @param endpointConfiguration
     * @return the constructed integration message.
     */
    Message convertInbound(WebServiceMessage webServiceMessage, MessageContext messageContext, WebServiceEndpointConfiguration endpointConfiguration);

}
