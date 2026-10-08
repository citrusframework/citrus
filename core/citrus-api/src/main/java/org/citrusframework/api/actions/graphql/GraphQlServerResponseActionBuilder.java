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
import org.citrusframework.api.actions.http.HttpSendResponseMessageBuilderFactory;
import org.citrusframework.api.actions.http.HttpServerResponseActionBuilder;
import org.citrusframework.api.graphql.GraphQlError;

public interface GraphQlServerResponseActionBuilder<T extends TestAction, M extends HttpSendResponseMessageBuilderFactory<T, M>, B extends HttpServerResponseActionBuilder<T, M, B>>
        extends HttpServerResponseActionBuilder<T, M, B> {

    /**
     * Replies with the given data object (JSON).
     */
    GraphQlServerResponseActionBuilder<T, M, B> data(String json);

    /**
     * Adds an entry to the {@code errors} array of the reply.
     */
    GraphQlServerResponseActionBuilder<T, M, B> error(GraphQlError error);

    /**
     * Enables or disables the GraphQL reply shape check for this action, overriding the endpoint setting.
     */
    GraphQlServerResponseActionBuilder<T, M, B> strict(boolean strict);
}
