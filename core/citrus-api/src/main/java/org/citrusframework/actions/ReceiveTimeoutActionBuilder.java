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

package org.citrusframework.actions;

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;

public interface ReceiveTimeoutActionBuilder<T extends TestAction>
        extends ActionBuilder<T, ReceiveTimeoutActionBuilder<T>>, TestActionBuilder<T> {

    /**
     * Sets the message endpoint to receive a timeout with.
     * @param messageEndpoint
     * @return
     */
    ReceiveTimeoutActionBuilder<T> endpoint(Endpoint messageEndpoint);

    /**
     * Sets the message endpoint uri to receive a timeout with.
     * @param messageEndpointUri
     * @return
     */
    ReceiveTimeoutActionBuilder<T> endpoint(String messageEndpointUri);

    /**
     * Sets time to wait for messages on destination.
     * @param timeout
     */
    ReceiveTimeoutActionBuilder<T> timeout(long timeout);

    /**
     * Adds message selector string for selective consumer.
     * @param messageSelector
     */
    ReceiveTimeoutActionBuilder<T> selector(String messageSelector);

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    ReceiveTimeoutActionBuilder<T> selector(Map<String, Object> messageSelector);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        ReceiveTimeoutActionBuilder<?> expectTimeout();

        default ReceiveTimeoutActionBuilder<?> receiveTimeout() {
            return expectTimeout();
        }

        default ReceiveTimeoutActionBuilder<?> expectTimeout(String endpointUri) {
            return receiveTimeout(endpointUri);
        }

        default ReceiveTimeoutActionBuilder<?> expectTimeout(Endpoint endpoint) {
            return receiveTimeout(endpoint);
        }

        default ReceiveTimeoutActionBuilder<?> receiveTimeout(String endpointUri) {
            return expectTimeout().endpoint(endpointUri);
        }

        default ReceiveTimeoutActionBuilder<?> receiveTimeout(Endpoint endpoint) {
            return expectTimeout().endpoint(endpoint);
        }

    }

}
