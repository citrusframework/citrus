/*
 * Copyright the original author or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelException;
import org.apache.camel.CamelExchangeException;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.support.DefaultExchange;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.message.MessageHeaderUtils;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.util.StringUtils;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

/**
 * Message converter able to read Camel exchange and create proper Spring Integration message
 * for internal use.
 *
 * @since 1.4.1
 */
public class CamelMessageConverter implements MessageConverter<Exchange, Exchange, CamelEndpointConfiguration> {

    @Override
    public Exchange convertOutbound(Message message, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        if (message.getPayload() instanceof Exchange exchange) {
            return exchange;
        }

        Exchange exchange = new DefaultExchange(endpointConfiguration.getCamelContext());
        convertOutbound(exchange, message, endpointConfiguration, context);
        return exchange;
    }

    @Override
    public void convertOutbound(Exchange exchange, Message message, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        org.apache.camel.Message in = exchange.getIn();
        for (Map.Entry<String, Object> header : message.getHeaders().entrySet()) {
            if (CamelSettings.isFilterInternalMessageHeaders() &&
                    !MessageHeaderUtils.isInternalMessageHeader(header.getKey())) {
                in.setHeader(header.getKey(), header.getValue());
            }

            switch (header.getKey()) {
                case CamelMessageHeaders.EXCHANGE_PATTERN: {
                    if (header.getValue() instanceof ExchangePattern pattern) {
                        exchange.setPattern(pattern);
                    } else if (header.getValue() instanceof String stringValue) {
                        exchange.setPattern(ExchangePattern.valueOf(stringValue));
                    }
                    break;
                }
                case CamelMessageHeaders.EXCHANGE_PROPERTIES: {
                    if (header.getValue() instanceof Map) {
                        ((Map<String, Object>) header.getValue()).forEach(exchange::setProperty);
                    } else if (header.getValue() instanceof String stringValue) {
                        StringUtils.extractMap(stringValue).forEach(exchange::setProperty);
                    }
                    break;
                }
                case CamelMessageHeaders.EXCHANGE_VARIABLES: {
                    if (header.getValue() instanceof Map) {
                        ((Map<String, Object>) header.getValue()).forEach(exchange::setVariable);
                    } else if (header.getValue() instanceof String stringValue) {
                        StringUtils.extractMap(stringValue).forEach(exchange::setVariable);
                    }
                    break;
                }
                case CamelMessageHeaders.EXCHANGE_EXCEPTION: {
                    if (header.getValue() instanceof Throwable exception) {
                        exchange.setException(exception);
                    } else if (header.getValue() instanceof String exceptionType) {
                        if (message.getHeaders().containsKey(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE)) {
                            if (CamelExchangeException.class.getName().equals(exceptionType)) {
                                exchange.setException(new CamelExchangeException(message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE).toString(), exchange));
                            } else {
                                // Instantiate the exception with given error message
                                exchange.setException(ClassLoaderHelper.instantiateType(exceptionType,
                                        message.getHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE).toString()));
                            }
                        } else {
                            if (CamelExchangeException.class.getName().equals(exceptionType)) {
                                exchange.setException(new CamelExchangeException("", exchange));
                            } else {
                                exchange.setException(ClassLoaderHelper.instantiateType(exceptionType));
                            }
                        }
                    }
                    break;
                }
                case CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE: {
                    if (!message.getHeaders().containsKey(CamelMessageHeaders.EXCHANGE_EXCEPTION)) {
                        exchange.setException(new CamelException(header.getValue().toString()));
                    }
                    break;
                }
            }
        }

        if (message.getPayload() instanceof Resource payloadResource) {
            // Camel does not know how to handle Citrus file resources, so convert to InputStream
            in.setBody(payloadResource.getInputStream());
        } else if (message.getPayload() instanceof Exchange sourceExchange) {
            if (!exchange.equals(sourceExchange)) {
                sourceExchange.getProperties().forEach(exchange::setProperty);
                sourceExchange.getVariables().forEach(exchange::setVariable);

                sourceExchange.getIn().getHeaders().forEach(exchange.getIn()::setHeader);
                in.setBody(sourceExchange.getIn().getBody());
            }
        } else {
            in.setBody(message.getPayload());
        }
    }

    @Override
    public Message convertInbound(Exchange exchange, CamelEndpointConfiguration endpointConfiguration, TestContext context) {
        if (exchange == null) {
            return null;
        }

        Message message = new DefaultMessage(exchange.getMessage().getBody(), exchange.getMessage().getHeaders())
                .setHeader(CamelMessageHeaders.EXCHANGE_ID, exchange.getExchangeId())
                .setHeader(CamelMessageHeaders.MESSAGE_ID, exchange.getMessage().getMessageId())
                .setHeader(CamelMessageHeaders.ROUTE_ID, exchange.getFromRouteId())
                .setHeader(CamelMessageHeaders.EXCHANGE_PATTERN, exchange.getPattern().name())
                .setHeader(CamelMessageHeaders.EXCHANGE_PROPERTIES, StringUtils.convertToString(exchange.getProperties()))
                .setHeader(CamelMessageHeaders.EXCHANGE_VARIABLES, StringUtils.convertToString(exchange.getVariables()))
                .setHeader(CamelMessageHeaders.EXCHANGE_FAILED, exchange.isFailed());

        //add all exchange properties as headers
        for (Map.Entry<String, Object> property : exchange.getProperties().entrySet()) {
            message.setHeader(property.getKey(), property.getValue());
        }

        //add all exchange variables as headers
        for (Map.Entry<String, Object> variable : exchange.getVariables().entrySet()) {
            message.setHeader(variable.getKey(), variable.getValue());
        }

        if (exchange.getException() != null) {
            message.setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION, exchange.getException().getClass().getName());
            message.setHeader(CamelMessageHeaders.EXCHANGE_EXCEPTION_MESSAGE, exchange.getException().getMessage());
        }

        return message;
    }

    /**
     * Converts a basic Java Map<String, String> into a mapping node.
     * Supports only String and boolean scalars as this is sufficient for data formats in Camel.
     */
    public static MappingNode createMappingNode(Map<?, ?> spec) {
        List<NodeTuple> tuples = new ArrayList<>();

        for (Map.Entry<?, ?> entry : spec.entrySet()) {
            // Create a ScalarNode for the key
            ScalarNode keyNode = new ScalarNode(Tag.STR, entry.getKey().toString(), ScalarStyle.PLAIN);

            ScalarNode valueNode;
            String stringValue = String.valueOf(entry.getValue());
            // Create a ScalarNode for the value
            if (stringValue.equals("true") || stringValue.equals("false")) {
                valueNode = new ScalarNode(Tag.BOOL, stringValue, ScalarStyle.PLAIN);
            } else {
                valueNode = new ScalarNode(Tag.STR, stringValue, ScalarStyle.PLAIN);
            }

            // Pair them together in a NodeTuple
            tuples.add(new NodeTuple(keyNode, valueNode));
        }

        // Return a new MappingNode holding the list of tuples
        // Tag.MAP indicates it's a map, and FlowStyle.BLOCK outputs it nicely formatted on separate lines
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }
}
