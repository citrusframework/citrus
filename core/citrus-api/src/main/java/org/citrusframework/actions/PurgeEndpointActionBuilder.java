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

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;

public interface PurgeEndpointActionBuilder<T extends TestAction>
        extends ActionBuilder<T, PurgeEndpointActionBuilder<T>>, TestActionBuilder<T>, ReferenceResolverAwareBuilder<T, PurgeEndpointActionBuilder<T>> {

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    PurgeEndpointActionBuilder<T> selector(String messageSelector);

    /**
     * Sets the messageSelector.
     * @param messageSelector the messageSelector to set
     */
    PurgeEndpointActionBuilder<T> selector(Map<String, Object> messageSelector);

    /**
     * Adds list of endpoint names to purge in this action.
     * @param endpointNames the endpointNames to set
     */
    PurgeEndpointActionBuilder<T> endpointNames(List<String> endpointNames);

    /**
     * Adds several endpoint names to the list of endpoints to purge in this action.
     * @param endpointNames
     * @return
     */
    PurgeEndpointActionBuilder<T> endpointNames(String... endpointNames);

    /**
     * Adds an endpoint name to the list of endpoints to purge in this action.
     * @param name
     * @return
     */
    PurgeEndpointActionBuilder<T> endpoint(String name);

    /**
     * Adds list of endpoints to purge in this action.
     * @param endpoints the endpoints to set
     */
    PurgeEndpointActionBuilder<T> endpoints(List<Endpoint> endpoints);

    /**
     * Sets several endpoints to purge in this action.
     * @param endpoints
     * @return
     */
    PurgeEndpointActionBuilder<T> endpoints(Endpoint... endpoints);

    /**
     * Adds an endpoint to the list of endpoints to purge in this action.
     * @param endpoint
     * @return
     */
    PurgeEndpointActionBuilder<T> endpoint(Endpoint endpoint);

    /**
     * Receive timeout for reading message from a destination.
     * @param receiveTimeout the receiveTimeout to set
     */
    PurgeEndpointActionBuilder<T> timeout(long receiveTimeout);

    /**
     * Sets the sleepTime.
     * @param millis the sleepTime to set
     */
    PurgeEndpointActionBuilder<T> sleep(long millis);

    interface BuilderFactory {

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        PurgeEndpointActionBuilder<?> purge();

        default PurgeEndpointActionBuilder<?> purgeEndpoints() {
            return purge();
        }

    }

}
