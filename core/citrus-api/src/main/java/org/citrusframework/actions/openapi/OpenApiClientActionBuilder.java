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
import org.citrusframework.actions.http.HttpReceiveResponseMessageBuilderFactory;
import org.citrusframework.actions.http.HttpSendRequestMessageBuilderFactory;
import org.citrusframework.endpoint.Endpoint;

public interface OpenApiClientActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>>
        extends ReferenceResolverAwareBuilder<T, B> {

    /**
     * Sets the  Http client.
     */
    OpenApiClientActionBuilder<T, B> client(String client);

    /**
     * Sets the  Http client.
     */
    OpenApiClientActionBuilder<T, B> client(Endpoint client);

    /**
     * Sends Http requests as client.
     */
    <M extends HttpSendRequestMessageBuilderFactory<T, M>> OpenApiClientRequestActionBuilder<T, M, ?> send(String operationKey);

    /**
     * Receives Http response messages as client.
     * Uses default Http status 200 OK.
     */
    <M extends HttpReceiveResponseMessageBuilderFactory<T, M>> OpenApiClientResponseActionBuilder<T, M, ?> receive(String operationKey);

    /**
     * Receives Http response messages as client with given status code.
     */
    <M extends HttpReceiveResponseMessageBuilderFactory<T, M>> OpenApiClientResponseActionBuilder<T, M, ?> receive(String operationKey, Object statusCode);

    /**
     * Receives Http response messages as client with given status code.
     */
    <M extends HttpReceiveResponseMessageBuilderFactory<T, M>> OpenApiClientResponseActionBuilder<T, M, ?> receive(String operationKey, int statusCode);

    /**
     * Receives Http response messages as client with given status.
     * Status may either refer to a status code number value or the status name (e.g. OK, NOT_FOUND)
     */
    <M extends HttpReceiveResponseMessageBuilderFactory<T, M>> OpenApiClientResponseActionBuilder<T, M, ?> receive(String operationKey, String statusCode);
}
