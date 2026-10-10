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
import org.citrusframework.api.actions.http.HttpClientResponseActionBuilder;
import org.citrusframework.api.actions.http.HttpReceiveResponseMessageBuilderFactory;
import org.citrusframework.api.graphql.GraphQlError;

public interface GraphQlClientResponseActionBuilder<T extends TestAction, M extends HttpReceiveResponseMessageBuilderFactory<T, M>, B extends HttpClientResponseActionBuilder<T, M, B>>
        extends HttpClientResponseActionBuilder<T, M, B> {

    /**
     * Expects a value in the response data; the JSONPath is relative to {@code $.data}.
     */
    GraphQlClientResponseActionBuilder<T, M, B> data(String path, Object expected);

    /**
     * Expects the response data object, compared as JSON.
     */
    GraphQlClientResponseActionBuilder<T, M, B> data(String json);

    /**
     * Expects the response to contain at least one error.
     */
    GraphQlClientResponseActionBuilder<T, M, B> expectErrors();

    /**
     * Expects the response to contain an error that matches every field set on the given error.
     */
    GraphQlClientResponseActionBuilder<T, M, B> expectError(GraphQlError error);

    /**
     * Enables or disables the GraphQL response shape check for this action, overriding the endpoint setting.
     */
    GraphQlClientResponseActionBuilder<T, M, B> strict(boolean strict);
}
