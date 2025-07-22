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

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.actions.SendMessageBuilderFactory;

public interface HttpSendRequestMessageBuilderFactory<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>>
        extends SendMessageBuilderFactory<T, M> {

    /**
     * Adds message payload multi value map data to this builder. This is used when using
     * multipart file upload via Spring RestTemplate.
     */
    HttpSendRequestMessageBuilderFactory<T, M> body(Map<String, List<Object>> payload);

    /**
     * Sets the request method.
     */
    HttpSendRequestMessageBuilderFactory<T, M> method(String method);

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used doesn't
     * provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    HttpSendRequestMessageBuilderFactory<T, M> uri(String uri);

    /**
     * Adds a query param to the request uri.
     */
    HttpSendRequestMessageBuilderFactory<T, M> queryParam(String name);

    /**
     * Adds a query param to the request uri.
     */
    HttpSendRequestMessageBuilderFactory<T, M> queryParam(String name, String value);

    /**
     * Sets the http version.
     */
    HttpSendRequestMessageBuilderFactory<T, M> version(String version);

    /**
     * Sets the request content type header.
     */
    HttpSendRequestMessageBuilderFactory<T, M> contentType(String contentType);

    /**
     * Sets the request accept header.
     */
    HttpSendRequestMessageBuilderFactory<T, M> accept(String accept);

    /**
     * Adds cookie to response by "Cookie" header.
     */
    HttpSendRequestMessageBuilderFactory<T, M> cookie(Object cookie);
}
