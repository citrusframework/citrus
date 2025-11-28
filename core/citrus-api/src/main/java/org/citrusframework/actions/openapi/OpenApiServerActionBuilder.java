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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.actions.http.HttpReceiveRequestMessageBuilderFactory;
import org.citrusframework.actions.http.HttpSendResponseMessageBuilderFactory;
import org.citrusframework.endpoint.Endpoint;

public interface OpenApiServerActionBuilder<T extends TestAction, B extends TestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Sets the  Http server.
     */
    OpenApiServerActionBuilder<T, B> server(String server);

    /**
     * Sets the  Http server.
     */
    OpenApiServerActionBuilder<T, B> server(Endpoint server);

    /**
     * Sends Http response messages as server. Uses default Http status 200 OK.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> OpenApiServerResponseActionBuilder<T, M, ?> send(String operationKey);

    /**
     * Send Http response messages as server to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> OpenApiServerResponseActionBuilder<T, M, ?> send(String operationKey, Object statusCode);

    /**
     * Send Http response messages as server to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> OpenApiServerResponseActionBuilder<T, M, ?> send(String operationKey, int statusCode);

    /**
     * Send Http response messages as server to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> OpenApiServerResponseActionBuilder<T, M, ?> send(String operationKey, String statusCode);

    /**
     * Send Http response messages as server to client.
     */
    <M extends HttpSendResponseMessageBuilderFactory<T, M>> OpenApiServerResponseActionBuilder<T, M, ?> send(String operationKey, String statusCode, String accept);

    /**
     * Receive Http requests as server.
     */
    <M extends HttpReceiveRequestMessageBuilderFactory<T, M>> OpenApiServerRequestActionBuilder<T, M, ?> receive(String operationKey);
}
