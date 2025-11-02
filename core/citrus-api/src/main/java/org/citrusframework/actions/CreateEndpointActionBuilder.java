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
import org.citrusframework.endpoint.EndpointBuilder;

public interface CreateEndpointActionBuilder<T extends TestAction>
        extends ActionBuilder<T, CreateEndpointActionBuilder<T>>, TestActionBuilder<T> {

    CreateEndpointActionBuilder<T> endpoint(EndpointBuilder<?> builder);

    CreateEndpointActionBuilder<T> uri(String endpointUri);

    CreateEndpointActionBuilder<T> type(String type);

    CreateEndpointActionBuilder<T> endpointName(String name);

    CreateEndpointActionBuilder<T> property(String name, String value);

    CreateEndpointActionBuilder<T> properties(Map<String, String> properties);

    interface BuilderFactory {

        CreateEndpointActionBuilder<?> createEndpoint();

        default CreateEndpointActionBuilder<?> createEndpoint(String endpointUri) {
            return createEndpoint().uri(endpointUri);
        }

        default CreateEndpointActionBuilder<?> createEndpoint(String type, Map<String, String> properties) {
            return createEndpoint()
                    .type(type)
                    .properties(properties);
        }
    }

}
