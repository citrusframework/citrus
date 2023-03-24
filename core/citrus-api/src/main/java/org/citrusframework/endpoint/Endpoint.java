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

package org.citrusframework.endpoint;

import org.citrusframework.TestActor;
import org.citrusframework.common.Named;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;

/**
 * Endpoint interface defines basic send and receive operations on a message endpoint.
 * @author Christoph Deppisch
 * @since 1.4
 */
public interface Endpoint extends Named {

    /**
     * Creates a message producer for this endpoint for sending messages
     * to this endpoint.
     */
    Producer createProducer();

    /**
     * Creates a message consumer for this endpoint. Consumer receives
     * messages on this endpoint.
     * @return
     */
    Consumer createConsumer();

    /**
     * Gets the endpoint configuration holding all endpoint specific properties such as
     * endpoint uri, connection timeout, ports, etc.
     * @return
     */
    EndpointConfiguration getEndpointConfiguration();

    /**
     * Gets the sending actor.
     * @return
     */
    TestActor getActor();

    /**
     * Sets the test actor for this endpoint.
     * @param actor
     */
    void setActor(TestActor actor);

    /**
     * Gets the endpoint name usually the Spring bean name.
     * @return
     */
    String getName();
}
