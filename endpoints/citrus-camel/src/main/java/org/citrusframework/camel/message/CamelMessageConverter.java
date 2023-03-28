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

package org.citrusframework.camel.message;

import java.util.Map;

import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;

/**
 * Message converter able to read Camel exchange and create proper Spring Integration message
 * for internal use.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelMessageConverter implements MessageConverter<Exchange, Exchange, CamelEndpointConfiguration> {

    @Override
    public Exchange convertOutbound(Message message, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        Exchange exchange = new DefaultExchange(endpointConfiguration.getCamelContext());
        convertOutbound(exchange, message, endpointConfiguration, context);
        return exchange;
    }

    @Override
    public void convertOutbound(Exchange exchange, Message message, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        org.apache.camel.Message in = exchange.getIn();
        for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
            in.setHeader(header.getKey(), header.getValue());
        }
        in.setBody(message.getPayload());
    }

    @Override
    public Message convertInbound(Exchange exchange, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        if (exchange == null) {
            return null;
        }

        Message message = new DefaultMessage(exchange.getMessage().getBody(), exchange.getMessage().getHeaders())
                .setHeader(CamelMessageHeaders.EXCHANGE_ID, exchange.getExchangeId())
                .setHeader(CamelMessageHeaders.ROUTE_ID, exchange.getFromRouteId())
                .setHeader(CamelMessageHeaders.EXCHANGE_PATTERN, exchange.getPattern().name())
                .setHeader(CamelMessageHeaders.EXCHANGE_FAILED, exchange.isFailed());

        //add all exchange properties
        for (Map.Entry<String, Object> property : exchange.getProperties().entrySet()) {
            message.setHeader(property.getKey(), property.getValue());
        }

        if (exchange.getException() != null) {
            message.setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, exchange.getException().getClass().getName());
            message.setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, exchange.getException().getMessage());
        }

        return message;
    }
}
