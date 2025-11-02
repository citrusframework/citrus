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

import org.citrusframework.endpoint.EndpointUriBuilder;
import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;

public class CamelEndpointBuilder extends AbstractEndpointBuilder<CamelEndpoint> {

    /** Endpoint target */
    private final CamelEndpoint endpoint = new CamelEndpoint();

    private String camelContext;
    private String messageConverter;

    @Override
    public CamelEndpoint build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(camelContext)) {
                camelContext(referenceResolver.resolve(camelContext, CamelContext.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, CamelMessageConverter.class));
            }
        }

        return super.build();
    }

    @Override
    protected CamelEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpointUri property.
     */
    public CamelEndpointBuilder endpointUri(String endpointUri) {
        endpoint.getEndpointConfiguration().setEndpointUri(endpointUri);
        return this;
    }

    @SchemaProperty(description = "The Camel endpoint uri.")
    public void setEndpointUri(String endpointUri) {
        endpointUri(endpointUri);
    }

    /**
     * Sets the endpoint uri from given builder.
     */
    public CamelEndpointBuilder endpoint(EndpointUriBuilder builder) {
        endpoint.getEndpointConfiguration().setEndpointUri(builder.getUri());
        return this;
    }

    /**
     * Sets the endpoint property.
     */
    public CamelEndpointBuilder endpoint(Endpoint camelEndpoint) {
        endpoint.getEndpointConfiguration().setEndpoint(camelEndpoint);
        return this;
    }

    /**
     * Sets the camelContext property.
     */
    public CamelEndpointBuilder camelContext(CamelContext camelContext) {
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
    public CamelEndpointBuilder messageConverter(CamelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message converter as a bean reference.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the default timeout.
     */
    public CamelEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The endpoint timeout when waiting for messages.")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
