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

import org.citrusframework.api.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.AbstractEndpoint;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.util.StringUtils;

/**
 * @since 1.4.1
 */
public class CamelEndpoint extends AbstractEndpoint implements InitializingPhase {

    /** Cached producer or consumer */
    private CamelConsumer camelConsumer;
    private CamelProducer camelProducer;

    /**
     * Default constructor initializes endpoint configuration;
     */
    public CamelEndpoint() {
        this(new CamelEndpointConfiguration());
    }

    /**
     * Default constructor using endpoint configuration.
     *
     * @param endpointConfiguration
     */
    public CamelEndpoint(CamelEndpointConfiguration endpointConfiguration) {
        super(endpointConfiguration);
    }

    @Override
    public Producer createProducer() {
        if (camelProducer == null) {
            camelProducer = new CamelProducer(getProducerName(), getEndpointConfiguration());
        }

        return camelProducer;
    }

    @Override
    public Consumer createConsumer() {
        if (camelConsumer == null) {
            camelConsumer = new CamelConsumer(getConsumerName(), getEndpointConfiguration());
        }

        return camelConsumer;
    }

    @Override
    public CamelEndpointConfiguration getEndpointConfiguration() {
        return (CamelEndpointConfiguration) super.getEndpointConfiguration();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void initialize(TestContext context) {
        if (StringUtils.hasText(getEndpointConfiguration().getEndpointUri())) {
            getEndpointConfiguration().setEndpointUri(
                    context.replaceDynamicContentInString(getEndpointConfiguration().getEndpointUri()));
        }
    }
}
