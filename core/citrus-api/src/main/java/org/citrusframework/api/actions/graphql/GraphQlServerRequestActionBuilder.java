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
import org.citrusframework.api.actions.http.HttpReceiveRequestMessageBuilderFactory;
import org.citrusframework.api.actions.http.HttpServerRequestActionBuilder;

public interface GraphQlServerRequestActionBuilder<T extends TestAction, M extends HttpReceiveRequestMessageBuilderFactory<T, M>, B extends HttpServerRequestActionBuilder<T, M, B>>
        extends HttpServerRequestActionBuilder<T, M, B> {

    /**
     * Expects the operation name.
     */
    GraphQlServerRequestActionBuilder<T, M, B> operationName(String operationName);

    /**
     * Expects the GraphQL document; compared ignoring formatting when strict, as text otherwise.
     */
    GraphQlServerRequestActionBuilder<T, M, B> query(String document);

    /**
     * Expects a variable value; validation matcher expressions are supported.
     */
    GraphQlServerRequestActionBuilder<T, M, B> variable(String name, Object expected);

    /**
     * Expects the variables object, compared as JSON.
     */
    GraphQlServerRequestActionBuilder<T, M, B> variables(String json);

    /**
     * Enables or disables GraphQL document checks for this action, overriding the endpoint setting.
     */
    GraphQlServerRequestActionBuilder<T, M, B> strict(boolean strict);
}
