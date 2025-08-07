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

public interface HttpClientRequestActionBuilder<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>, B extends HttpClientRequestActionBuilder<T, M, B>>
        extends SendActionBuilder<T, M, B> {

    /**
     * Sets the request path.
     */
    B path(String path);

    /**
     * Sets the request method.
     */
    B method(String method);

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used doesn't
     * provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    B uri(String uri);

    /**
     * Adds a query param to the request uri.
     */
    B queryParam(String name);

    /**
     * Adds a query param to the request uri.
     */
    B queryParam(String name, String value);
}
