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
import org.citrusframework.actions.ReceiveMessageBuilderFactory;

public interface HttpReceiveRequestMessageBuilderFactory<T extends TestAction, M extends HttpReceiveRequestMessageBuilderFactory<T, M>>
        extends ReceiveMessageBuilderFactory<T, M> {

    /**
     * Adds message payload multi value map data to this builder. This is used when using multipart file upload via
     * Spring RestTemplate.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> body(Map<String, List<Object>> payload);

    /**
     * Sets the request method.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> method(String method);

    /**
     * Adds a query param to the request uri.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> queryParam(String name);

    /**
     * Adds a query param to the request uri.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> queryParam(String name, String value);

    /**
     * Sets the http version.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> version(String version);

    /**
     * Sets the request content type header.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> contentType(String contentType);

    /**
     * Expects cookie on response via "Set-Cookie" header.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> cookie(Object o);

    /**
     * Sets the request accept header.
     */
    HttpReceiveRequestMessageBuilderFactory<T, M> accept(String accept);
}
