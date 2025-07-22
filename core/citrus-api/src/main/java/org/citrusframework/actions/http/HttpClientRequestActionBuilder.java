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
import org.citrusframework.actions.SendActionBuilder;

public interface HttpClientRequestActionBuilder<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>>
        extends SendActionBuilder<T, M> {

    /**
     * Sets the request path.
     */
    HttpClientRequestActionBuilder<T, M> path(String path);

    /**
     * Sets the request method.
     */
    HttpClientRequestActionBuilder<T, M> method(String method);

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used doesn't
     * provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    HttpClientRequestActionBuilder<T, M> uri(String uri);

    /**
     * Adds a query param to the request uri.
     */
    HttpClientRequestActionBuilder<T, M> queryParam(String name);

    /**
     * Adds a query param to the request uri.
     */
    HttpClientRequestActionBuilder<T, M> queryParam(String name, String value);
}
