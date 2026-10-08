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
package org.citrusframework.api.actions.graphql;

import org.citrusframework.TestAction;
import org.citrusframework.api.actions.http.HttpClientRequestActionBuilder;
import org.citrusframework.api.actions.http.HttpSendRequestMessageBuilderFactory;
import org.citrusframework.spi.Resource;

public interface GraphQlClientRequestActionBuilder<T extends TestAction, M extends HttpSendRequestMessageBuilderFactory<T, M>, B extends HttpClientRequestActionBuilder<T, M, B>>
        extends HttpClientRequestActionBuilder<T, M, B> {

    /**
     * Sets the GraphQL document to send.
     */
    GraphQlClientRequestActionBuilder<T, M, B> query(String document);

    /**
     * Loads the GraphQL document to send from a resource path, e.g. {@code classpath:graphql/book.graphql}.
     */
    GraphQlClientRequestActionBuilder<T, M, B> queryResource(String resourcePath);

    /**
     * Loads the GraphQL document to send from a resource.
     */
    GraphQlClientRequestActionBuilder<T, M, B> queryResource(Resource resource);

    /**
     * Sets the name of the operation to execute.
     */
    GraphQlClientRequestActionBuilder<T, M, B> operationName(String operationName);

    /**
     * Adds a variable; numbers, booleans, lists and maps keep their JSON type, strings stay strings.
     */
    GraphQlClientRequestActionBuilder<T, M, B> variable(String name, Object value);

    /**
     * Sets the variables as JSON object; Citrus expressions are resolved before parsing.
     */
    GraphQlClientRequestActionBuilder<T, M, B> variables(String json);

    /**
     * Sends the operation as HTTP GET with query parameters.
     */
    GraphQlClientRequestActionBuilder<T, M, B> get();

    /**
     * Sends the operation as HTTP POST with a JSON body.
     */
    GraphQlClientRequestActionBuilder<T, M, B> post();

    /**
     * Enables or disables GraphQL document checks for this action, overriding the endpoint setting.
     */
    GraphQlClientRequestActionBuilder<T, M, B> strict(boolean strict);
}
