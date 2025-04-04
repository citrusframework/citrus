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

package org.citrusframework.http.client;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

/**
 * @since 2.7
 */
public class HttpResponseErrorHandler extends DefaultResponseErrorHandler {

    private final ErrorHandlingStrategy errorHandlingStrategy;

    /**
     * Default constructor using error handling strategy.
     */
    public HttpResponseErrorHandler(ErrorHandlingStrategy errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
    }

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatusCode statusCode, @Nullable URI url, @Nullable HttpMethod method) throws IOException {
        if (errorHandlingStrategy.equals(ErrorHandlingStrategy.PROPAGATE)) {
            throw new HttpErrorPropagatingException(response.getStatusCode(), response.getStatusText(), response.getHeaders(), getResponseBody(response), getCharset(response));
        } else if (errorHandlingStrategy.equals(ErrorHandlingStrategy.THROWS_EXCEPTION)) {
            super.handleError(response, statusCode, url, method);
        } else {
            throw new CitrusRuntimeException("Unsupported error strategy: " + errorHandlingStrategy);
        }
    }
}
