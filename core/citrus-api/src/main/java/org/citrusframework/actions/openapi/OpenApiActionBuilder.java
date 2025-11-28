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

package org.citrusframework.actions.openapi;

import java.net.URL;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.openapi.Specification;

public interface OpenApiActionBuilder<T extends TestAction, S extends Specification, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    OpenApiActionBuilder<T, S, B> alias(String openApiAlias);

    OpenApiActionBuilder<T, S, B> specification(S spec);

    OpenApiActionBuilder<T, S, B> specification(URL specUrl);

    OpenApiActionBuilder<T, S, B> specification(String specUrl);

    default OpenApiActionBuilder<T, S, B> specification(Object spec) {
        if (spec instanceof Specification) {
            return specification((S) spec);
        } else if (spec instanceof URL) {
            return specification((URL) spec);
        } else if (spec instanceof String) {
            return specification((String) spec);
        } else {
            throw new IllegalArgumentException("Invalid OpenApi specification type '%s'".formatted(spec.getClass().getName()));
        }
    }

    /**
     * Initiate http client action.
     */
    OpenApiClientActionBuilder<?, ?> client();

    /**
     * Initiate http client action.
     */
    OpenApiClientActionBuilder<?, ?> client(Endpoint endpoint);

    /**
     * Initiate http client action.
     */
    OpenApiClientActionBuilder<?, ?> client(String endpoint);

    /**
     * Initiate http server action.
     */
    OpenApiServerActionBuilder<?, ?> server();

    /**
     * Initiate http server action.
     */
    OpenApiServerActionBuilder<?, ?> server(Endpoint endpoint);

    /**
     * Initiate http server action.
     */
    OpenApiServerActionBuilder<?, ?> server(String endpoint);

    interface BuilderFactory {

        OpenApiActionBuilder<?, ?, ?> openapi();

        default OpenApiActionBuilder<?, ?, ?> openapi(Specification specification) {
            return openapi().specification(specification);
        }

    }
}
