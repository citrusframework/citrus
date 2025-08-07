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

public interface HttpServerActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Send Http response messages as server to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> HttpServerSendActionBuilder<T, M, ?> send();

    /**
     * Receive Http requests as server.
     */
    <M extends HttpReceiveRequestMessageBuilderFactory<T, M>> HttpServerReceiveActionBuilder<T, M, ?> receive();

    /**
     * Generic request builder with request method and path.
     */
    <M extends HttpReceiveRequestMessageBuilderFactory<T, M>> HttpServerRequestActionBuilder<T, M, ?> request(String method, String path);

    /**
     * Generic response builder for sending response messages to client with response status code.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> HttpServerResponseActionBuilder<T, M, ?> respond(int status);

    /**
     * Generic response builder for sending response messages to client with response status code.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> HttpServerResponseActionBuilder<T, M, ?> respond(Object status);

    /**
     * Generic response builder for sending response messages to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> HttpServerResponseActionBuilder<T, M, ?> respond();

    /**
     * Generic response builder for sending JSON response messages to client with response status 200 (OK).
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> M respondOkJson(String json);

    /**
     * Generic response builder for sending JSON response messages to client with response status 200 (OK).
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> M respondOkJson(Object json);

}
