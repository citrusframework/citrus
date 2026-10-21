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

package org.citrusframework.http.interceptor;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;

/**
 * Interceptor that handles a delivered request around the Citrus request handling.
 * <p>
 * Interceptors are transport neutral and are supported by all HTTP server implementations.
 * They are invoked in configuration order, the first configured interceptor being the outermost
 * one. An interceptor may
 * <ul>
 *     <li>short circuit the request handling by returning a reply without calling
 *         {@link Chain#proceed(HttpMessage)} - the test never receives the request</li>
 *     <li>change the request that is passed to the next interceptor or the endpoint adapter</li>
 *     <li>replace or decorate the reply that is returned by the next interceptor or the
 *         endpoint adapter</li>
 * </ul>
 * When an interceptor raises an exception the server answers the client with an internal
 * server error.
 *
 * @since 5.1.0
 */
@FunctionalInterface
public interface HttpServerInterceptor {

    /**
     * Intercepts the given request and handles it.
     *
     * @param request the request that reached the server.
     * @param chain the chain of interceptors and the endpoint adapter.
     * @return the reply to send to the client or {@code null} for no reply (empty body with
     *         the default status code).
     */
    Message intercept(HttpMessage request, Chain chain);

    /**
     * Chain of interceptors ending with the endpoint adapter.
     */
    interface Chain {

        /**
         * Passes the (possibly changed) request to the next interceptor or the endpoint adapter.
         *
         * @param request the request to handle.
         * @return the reply of the next interceptor or the endpoint adapter.
         */
        Message proceed(HttpMessage request);
    }
}
