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

import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.apache.camel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelProducer implements Producer {

    /** The producer name. */
    private final String name;

    /** Endpoint configuration */
    private final CamelEndpointConfiguration endpointConfiguration;

    /** Cached producer template - only created once for this producer */
    private ProducerTemplate producerTemplate;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelProducer.class);

    /**
     * Constructor using endpoint configuration and fields.
     * @param name
     * @param endpointConfiguration
     */
    public CamelProducer(String name, CamelEndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public void send(final Message message, final TestContext context) {
        String endpointUri;
        if (endpointConfiguration.getEndpointUri() != null) {
            endpointUri = context.replaceDynamicContentInString(endpointConfiguration.getEndpointUri());
        } else if (endpointConfiguration.getEndpoint() != null) {
            endpointUri = endpointConfiguration.getEndpoint().getEndpointUri();
        } else {
            throw new CitrusRuntimeException("Missing endpoint or endpointUri on Camel producer");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sending message to camel endpoint: '" + endpointUri + "'");
        }

        Exchange camelExchange;
        if (endpointConfiguration.getEndpoint() != null) {
            camelExchange = getProducerTemplate(context)
                    .send(endpointConfiguration.getEndpoint(), exchange ->
                            endpointConfiguration.getMessageConverter().convertOutbound(exchange, message, endpointConfiguration, context));
        } else {
            camelExchange = getProducerTemplate(context)
                    .send(endpointUri, exchange ->
                            endpointConfiguration.getMessageConverter().convertOutbound(exchange, message, endpointConfiguration, context));
        }

        if (camelExchange.getException() != null) {
            throw new CitrusRuntimeException("Sending message to camel endpoint resulted in exception", camelExchange.getException());
        }

        context.onOutboundMessage(message);

        logger.info("Message was sent to camel endpoint '" + endpointUri + "'");
    }

    /**
     * Creates new producer template if not present yet. Create producer template only once which is
     * mandatory for direct endpoints that do only support one single producer at a time.
     * @param context
     * @return
     */
    protected ProducerTemplate getProducerTemplate(TestContext context) {
        if (producerTemplate == null) {
            if (endpointConfiguration.getCamelContext() != null) {
                producerTemplate = endpointConfiguration.getCamelContext().createProducerTemplate();
            } else if (context.getReferenceResolver() != null) {
                if (context.getReferenceResolver().resolveAll(CamelContext.class).size() == 1) {
                    endpointConfiguration.setCamelContext(context.getReferenceResolver().resolve(CamelContext.class));
                } else if (context.getReferenceResolver().isResolvable("camelContext")) {
                    endpointConfiguration.setCamelContext(context.getReferenceResolver().resolve("camelContext", CamelContext.class));
                } else {
                    endpointConfiguration.setCamelContext(new DefaultCamelContext());
                }

                producerTemplate = endpointConfiguration.getCamelContext().createProducerTemplate();
            }
        }

        return producerTemplate;
    }

    @Override
    public String getName() {
        return name;
    }
}
