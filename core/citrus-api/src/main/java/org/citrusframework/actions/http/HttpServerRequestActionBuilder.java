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
import org.citrusframework.actions.ReceiveActionBuilder;

public interface HttpServerRequestActionBuilder<T extends TestAction, M extends HttpReceiveRequestMessageBuilderFactory<T, M>>
        extends ReceiveActionBuilder<T, M> {

    /**
     * Sets the request path.
     */
    HttpServerRequestActionBuilder<T, M> path(String path);

    /**
     * Sets the request method.
     */
    HttpServerRequestActionBuilder<T, M> method(String method);

    /**
     * Adds a query param to the request uri.
     */
    HttpServerRequestActionBuilder<T, M> queryParam(String name);

    /**
     * Adds a query param to the request uri.
     */
    HttpServerRequestActionBuilder<T, M> queryParam(String name, String value);
}
