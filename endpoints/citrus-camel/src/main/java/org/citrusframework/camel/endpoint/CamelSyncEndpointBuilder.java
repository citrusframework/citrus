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

package org.citrusframework.camel.endpoint;

import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.message.MessageCorrelator;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

/**
 * @since 2.5
 */
public class CamelSyncEndpointBuilder extends AbstractEndpointBuilder<CamelSyncEndpoint> {

    /** Endpoint target */
    private final CamelSyncEndpoint endpoint = new CamelSyncEndpoint();

    private String camelContext;
    private String messageConverter;
    private String correlator;

    @Override
    public CamelSyncEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(camelContext)) {
                camelContext(referenceResolver.resolve(camelContext, CamelContext.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, CamelMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }
        }

        return super.build();
    }

    @Override
    protected CamelSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpointUri property.
     */
    public CamelSyncEndpointBuilder endpointUri(String endpointUri) {
        endpoint.getEndpointConfiguration().setEndpointUri(endpointUri);
        return this;
    }

    @SchemaProperty(description = "The Camel endpoint uri.")
    public void setEndpointUri(String endpointUri) {
        endpointUri(endpointUri);
    }

    /**
     * Sets the endpoint property.
     */
    public CamelSyncEndpointBuilder endpoint(Endpoint camelEndpoint) {
        endpoint.getEndpointConfiguration().setEndpoint(camelEndpoint);
        return this;
    }

    /**
     * Sets the camelContext property.
     */
    public CamelSyncEndpointBuilder camelContext(CamelContext camelContext) {
        endpoint.getEndpointConfiguration().setCamelContext(camelContext);
        return this;
    }

    @SchemaProperty(description = "The Camel context to use.")
    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Sets the messageConverter property.
     */
    public CamelSyncEndpointBuilder messageConverter(CamelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the polling interval.
     */
    public CamelSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the message correlator.
     */
    public CamelSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the default timeout.
     */
    public CamelSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
