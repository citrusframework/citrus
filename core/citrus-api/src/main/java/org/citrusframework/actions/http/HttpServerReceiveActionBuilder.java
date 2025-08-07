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

public interface HttpServerReceiveActionBuilder<T extends TestAction, M extends HttpReceiveRequestMessageBuilderFactory<T, M>, B extends HttpServerRequestActionBuilder<T, M, B>> {

    /**
     * Receives Http GET request as client to server.
     */
    B get();

    /**
     * Receives Http GET request as client to server.
     */
    B get(String path);

    /**
     * Receives Http POST request as client to server.
     */
    B post();

    /**
     * Receives Http POST request as client to server.
     */
    B post(String path);

    /**
     * Receives Http PUT request as client to server.
     */
    B put();

    /**
     * Receives Http PUT request as client to server.
     */
    B put(String path);

    /**
     * Receives Http DELETE request as client to server.
     */
    B delete();

    /**
     * Receives Http DELETE request as client to server.
     */
    B delete(String path);

    /**
     * Receives Http HEAD request as client to server.
     */
    B head();

    /**
     * Receives Http HEAD request as client to server.
     */
    B head(String path);

    /**
     * Receives Http OPTIONS request as client to server.
     */
    B options();

    /**
     * Receives Http OPTIONS request as client to server.
     */
    B options(String path);

    /**
     * Receives Http TRACE request as client to server.
     */
    B trace();

    /**
     * Receives Http TRACE request as client to server.
     */
    B trace(String path);

    /**
     * Receives Http PATCH request as client to server.
     */
    B patch();

    /**
     * Receives Http PATCH request as client to server.
     */
    B patch(String path);
}
