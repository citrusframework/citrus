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
import org.citrusframework.actions.SendMessageBuilderFactory;

public interface HttpSendResponseMessageBuilderFactory<T extends TestAction, M extends HttpSendResponseMessageBuilderFactory<T, M>>
        extends SendMessageBuilderFactory<T, M> {

    /**
     * Sets the response status.
     */
    HttpSendResponseMessageBuilderFactory<T, M> status(int status);

    /**
     * Sets the response status.
     */
    HttpSendResponseMessageBuilderFactory<T, M> status(Object status);

    /**
     * Sets the response status code.
     */
    HttpSendResponseMessageBuilderFactory<T, M> statusCode(int statusCode);

    /**
     * Sets the response reason phrase.
     */
    HttpSendResponseMessageBuilderFactory<T, M> reasonPhrase(String reasonPhrase);

    /**
     * Sets the http version.
     */
    HttpSendResponseMessageBuilderFactory<T, M> version(String version);

    /**
     * Sets the request content type header.
     */
    HttpSendResponseMessageBuilderFactory<T, M> contentType(String contentType);

    /**
     * Adds cookie to response by "Set-Cookie" header.
     */
    HttpSendResponseMessageBuilderFactory<T, M> cookie(Object o);
}
