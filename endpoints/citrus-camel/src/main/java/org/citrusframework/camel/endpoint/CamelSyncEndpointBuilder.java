/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class CamelSyncEndpointBuilder extends AbstractEndpointBuilder<CamelSyncEndpoint> {

    /** Endpoint target */
    private final CamelSyncEndpoint endpoint = new CamelSyncEndpoint();

    @Override
    protected CamelSyncEndpoint getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the endpointUri property.
     * @param endpointUri
     * @return
     */
    public CamelSyncEndpointBuilder endpointUri(String endpointUri) {
        endpoint.getEndpointConfiguration().setEndpointUri(endpointUri);
        return this;
    }

    /**
     * Sets the endpoint property.
     * @param camelEndpoint
     * @return
     */
    public CamelSyncEndpointBuilder endpoint(Endpoint camelEndpoint) {
        endpoint.getEndpointConfiguration().setEndpoint(camelEndpoint);
        return this;
    }

    /**
     * Sets the camelContext property.
     * @param camelContext
     * @return
     */
    public CamelSyncEndpointBuilder camelContext(CamelContext camelContext) {
        endpoint.getEndpointConfiguration().setCamelContext(camelContext);
        return this;
    }

    /**
     * Sets the messageConverter property.
     * @param messageConverter
     * @return
     */
    public CamelSyncEndpointBuilder messageConverter(CamelMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public CamelSyncEndpointBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public CamelSyncEndpointBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public CamelSyncEndpointBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
