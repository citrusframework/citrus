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

import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultExchange;
import org.citrusframework.camel.CamelSettings;
import org.citrusframework.camel.endpoint.CamelEndpointConfiguration;
import org.citrusframework.context.TestContext;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageConverter;
import org.citrusframework.message.MessageHeaderUtils;
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
