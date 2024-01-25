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

package org.citrusframework.camel.endpoint;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelConsumer implements Consumer {
    /** Endpoint configuration */
    private final CamelEndpointConfiguration endpointConfiguration;

    /** The consumer name */
    private final String name;

    /** Cached consumer template - only created once for this consumer */
    private ConsumerTemplate consumerTemplate;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelConsumer.class);

    /**
     * Constructor using endpoint configuration and fields.
     * @param name
     * @param endpointConfiguration
     */
    public CamelConsumer(String name, CamelEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(TestContext context) {
        return receive(context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        String endpointUri;
        if (endpointConfiguration.getEndpointUri() != null) {
            endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri());
        } else if (endpointConfiguration.getEndpoint() != null) {
            endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
        } else {
            throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel consumer");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Receiving message from camel endpoint: '" + endpointUri + "'");
        }

        Exchange exchange;
        if (endpointConfiguration.getEndpoint() != null) {
            exchange = getConsumerTemplate(context).receive(endpointConfiguration.getEndpoint(), timeout);
        } else {
            exchange = getConsumerTemplate(context).receive(endpointUri, timeout);
        }

        if (exchange == null) {
            throw new MessageTimeoutException(timeout, endpointUri);
        }

        logger.info("Received message from camel endpoint: '" + endpointUri + "'");

        Message message = endpointConfiguration.getMessageConverter().convertInbound(exchange, endpointConfiguration, context);
        context.onInboundMessage(message);

        return message;
    }

    /**
     * Creates new consumer template if not present yet. Create consumer template only once which is
     * mandatory for direct endpoints that do only support one single consumer at a time.
     * @param context
     * @return
     */
    protected ConsumerTemplate getConsumerTemplate(TestContext context) {
        if (consumerTemplate == null) {
            if (endpointConfiguration.getCamelContext() != null) {
                consumerTemplate = endpointConfiguration.getCamelContext().createConsumerTemplate();
            } else {
                if (context.getReferenceResolver().resolveAll(CamelContext.class).size() == 1) {
                    endpointConfiguration.setCamelContext(context.getReferenceResolver().resolve(CamelContext.class));
                } else if (context.getReferenceResolver().isResolvable("camelContext")) {
                    endpointConfiguration.setCamelContext(context.getReferenceResolver().resolve("camelContext", CamelContext.class));
                } else {
                    endpointConfiguration.setCamelContext(new DefaultCamelContext());
                }

                consumerTemplate = endpointConfiguration.getCamelContext().createConsumerTemplate();
            }
        }

        return consumerTemplate;
    }

    @Override
    public String getName() {
        return name;
    }

}
