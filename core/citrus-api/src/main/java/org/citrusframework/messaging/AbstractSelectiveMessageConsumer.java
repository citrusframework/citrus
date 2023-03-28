/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.messaging;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.Message;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractSelectiveMessageConsumer extends AbstractMessageConsumer implements SelectiveConsumer {

    /** Endpoint configuration */
    private final EndpointConfiguration endpointConfiguration;

    /**
     * Default constructor using receive timeout setting.
     *
     * @param name
     * @param endpointConfiguration
     */
    public AbstractSelectiveMessageConsumer(String name, EndpointConfiguration endpointConfiguration) {
        super(name, endpointConfiguration);
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public Message receive(String selector, TestContext context) {
        return receive(selector, context, endpointConfiguration.getTimeout());
    }

    @Override
    public Message receive(TestContext context, long timeout) {
        return receive(null, context, timeout);
    }
}
