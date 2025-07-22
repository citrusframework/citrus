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

public interface HttpServerReceiveActionBuilder<T extends TestAction, M extends HttpReceiveRequestMessageBuilderFactory<T, M>> {

    /**
     * Receives Http GET request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> get();

    /**
     * Receives Http GET request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> get(String path);

    /**
     * Receives Http POST request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> post();

    /**
     * Receives Http POST request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> post(String path);

    /**
     * Receives Http PUT request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> put();

    /**
     * Receives Http PUT request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> put(String path);

    /**
     * Receives Http DELETE request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> delete();

    /**
     * Receives Http DELETE request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> delete(String path);

    /**
     * Receives Http HEAD request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> head();

    /**
     * Receives Http HEAD request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> head(String path);

    /**
     * Receives Http OPTIONS request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> options();

    /**
     * Receives Http OPTIONS request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> options(String path);

    /**
     * Receives Http TRACE request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> trace();

    /**
     * Receives Http TRACE request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> trace(String path);

    /**
     * Receives Http PATCH request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> patch();

    /**
     * Receives Http PATCH request as client to server.
     */
    HttpServerRequestActionBuilder<T, M> patch(String path);
}
