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

import org.citrusframework.camel.message.CamelMessageConverter;
import org.citrusframework.endpoint.AbstractEndpointConfiguration;
import org.apache.camel.*;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class CamelEndpointConfiguration extends AbstractEndpointConfiguration {

    /** Camel context this endpoint is working with */
    private CamelContext camelContext;

    /** Message converter */
    private CamelMessageConverter messageConverter = new CamelMessageConverter();

    /** Camel endpoint uri */
    private String endpointUri;

    /** Camel endpoint as destination */
    private Endpoint endpoint;

    /**
     * Gets the Camel context.
     * @return
     */
    public CamelContext getCamelContext() {
        return camelContext;
    }

    /**
     * Sets the Camel context.
     * @param camelContext
     */
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    /**
     * Gets the endpoint uri.
     * @return
     */
    public String getEndpointUri() {
        return endpointUri;
    }

    /**
     * Sets the endpoint uri.
     * @param endpointUri
     */
    public void setEndpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
    }

    /**
     * Gets the endpoint.
     * @return
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpoint.
     * @param endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public CamelMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(CamelMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
