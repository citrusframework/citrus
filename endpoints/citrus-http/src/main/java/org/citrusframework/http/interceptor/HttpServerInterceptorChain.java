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

import java.util.List;
import java.util.function.Function;

import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;

/**
 * Chain of {@link HttpServerInterceptor}s that ends with the endpoint adapter handling the
 * request. Interceptors are invoked in configuration order, the first configured interceptor
 * being the outermost one.
 *
 * @since 5.1.0
 */
public final class HttpServerInterceptorChain {

    /** The configured interceptors */
    private final List<HttpServerInterceptor> interceptors;

    /** The endpoint adapter handling the request */
    private final Function<HttpMessage, Message> endpointHandler;

    /**
     * Default constructor.
     *
     * @param interceptors the configured interceptors.
     * @param endpointHandler the endpoint adapter handling the request.
     */
    public HttpServerInterceptorChain(List<HttpServerInterceptor> interceptors, Function<HttpMessage, Message> endpointHandler) {
        this.interceptors = interceptors != null ? interceptors : List.of();
        this.endpointHandler = endpointHandler;
    }

    /**
     * Passes the request to the first interceptor and eventually to the endpoint adapter.
     *
     * @param request the request to handle.
     * @return the reply of the last interceptor or the endpoint adapter.
     */
    public Message proceed(HttpMessage request) {
        return proceed(request, 0);
    }

    /**
     * Passes the request to the interceptor at the given index and eventually to the endpoint adapter.
     *
     * @param request the request to handle.
     * @param index the index of the interceptor to invoke.
     * @return the reply of the last interceptor or the endpoint adapter.
     */
    private Message proceed(HttpMessage request, int index) {
        if (index >= interceptors.size()) {
            return endpointHandler.apply(request);
        }

        HttpServerInterceptor interceptor = interceptors.get(index);
        return interceptor.intercept(request, (HttpMessage current) -> proceed(current, index + 1));
    }
}
