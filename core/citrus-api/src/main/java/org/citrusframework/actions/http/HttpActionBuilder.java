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

package org.citrusframework.actions.http;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.endpoint.Endpoint;

public interface HttpActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Initiate http client action.
     */
    HttpClientActionBuilder<?, ?> client();

    /**
     * Initiate http client action.
     */
    HttpClientActionBuilder<?, ?> client(Endpoint endpoint);

    /**
     * Initiate http client action.
     */
    HttpClientActionBuilder<?, ?> client(String endpoint);

    /**
     * Initiate http server action.
     */
    HttpServerActionBuilder<?, ?> server();

    /**
     * Initiate http server action.
     */
    HttpServerActionBuilder<?, ?> server(Endpoint endpoint);

    /**
     * Initiate http server action.
     */
    HttpServerActionBuilder<?, ?> server(String endpoint);

    interface BuilderFactory {

        HttpActionBuilder<?, ?> http();

    }
}
