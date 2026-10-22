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

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Exception thrown when a Citrus server interceptor fails while handling a request. The exception
 * separates a failure of the interceptor itself from a failure of the endpoint adapter handling the
 * request. Only the interceptor failure is translated into a server error response by the server
 * implementations. A failure of the endpoint adapter is propagated to the caller of the request.
 *
 * @since 5.2
 */
public class HttpServerInterceptorException extends CitrusRuntimeException {

    /** The interceptor that failed */
    private final transient HttpServerInterceptor interceptor;

    /**
     * Constructor using the failing interceptor and the cause of the failure.
     *
     * @param interceptor the interceptor that failed.
     * @param cause the cause of the failure.
     */
    public HttpServerInterceptorException(HttpServerInterceptor interceptor, Throwable cause) {
        super("Server interceptor %s failed: %s"
                .formatted(interceptor != null ? interceptor.getClass().getName() : "unknown", cause.getMessage()), cause);
        this.interceptor = interceptor;
    }

    /**
     * Gets the interceptor that failed.
     *
     * @return the interceptor.
     */
    public HttpServerInterceptor getInterceptor() {
        return interceptor;
    }
}
