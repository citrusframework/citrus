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

public interface HttpClientSendActionBuilder<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>> {

    /**
     * Sends Http GET request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> get();

    /**
     * Sends Http GET request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> get(String path);

    /**
     * Sends Http POST request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> post();

    /**
     * Sends Http POST request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> post(String path);

    /**
     * Sends Http PUT request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> put();

    /**
     * Sends Http PUT request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> put(String path);

    /**
     * Sends Http DELETE request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> delete();

    /**
     * Sends Http DELETE request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> delete(String path);

    /**
     * Sends Http HEAD request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> head();

    /**
     * Sends Http HEAD request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> head(String path);

    /**
     * Sends Http OPTIONS request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> options();

    /**
     * Sends Http OPTIONS request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> options(String path);

    /**
     * Sends Http TRACE request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> trace();

    /**
     * Sends Http TRACE request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> trace(String path);

    /**
     * Sends Http PATCH request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> patch();

    /**
     * Sends Http PATCH request as client to server.
     */
    HttpClientRequestActionBuilder<T, M> patch(String path);
}
