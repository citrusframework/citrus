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
public abstract class AbstractMessageConsumer implements Consumer {

    /** Endpoint configuration */
    private final EndpointConfiguration endpointConfiguration;

    /** The consumer name */
    private final String name;

    /**
     * Default constructor using receive timeout setting.
     * @param name
     * @param endpointConfiguration
     */
    public AbstractMessageConsumer(String name, EndpointConfiguration endpointConfiguration) {
        this.name = name;
        this.endpointConfiguration = endpointConfiguration;
    }

    @Override
    public String getName() {
        return name;
    }

    public Message receive(TestContext context) {
        return receive(context, endpointConfiguration.getTimeout());
    }
}
