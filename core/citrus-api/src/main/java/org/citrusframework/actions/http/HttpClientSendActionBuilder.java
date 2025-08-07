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

public interface HttpClientSendActionBuilder<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>, B extends HttpClientRequestActionBuilder<T, M, B>> {

    /**
     * Sends Http GET request as client to server.
     */
    B get();

    /**
     * Sends Http GET request as client to server.
     */
    B get(String path);

    /**
     * Sends Http POST request as client to server.
     */
    B post();

    /**
     * Sends Http POST request as client to server.
     */
    B post(String path);

    /**
     * Sends Http PUT request as client to server.
     */
    B put();

    /**
     * Sends Http PUT request as client to server.
     */
    B put(String path);

    /**
     * Sends Http DELETE request as client to server.
     */
    B delete();

    /**
     * Sends Http DELETE request as client to server.
     */
    B delete(String path);

    /**
     * Sends Http HEAD request as client to server.
     */
    B head();

    /**
     * Sends Http HEAD request as client to server.
     */
    B head(String path);

    /**
     * Sends Http OPTIONS request as client to server.
     */
    B options();

    /**
     * Sends Http OPTIONS request as client to server.
     */
    B options(String path);

    /**
     * Sends Http TRACE request as client to server.
     */
    B trace();

    /**
     * Sends Http TRACE request as client to server.
     */
    B trace(String path);

    /**
     * Sends Http PATCH request as client to server.
     */
    B patch();

    /**
     * Sends Http PATCH request as client to server.
     */
    B patch(String path);
}
