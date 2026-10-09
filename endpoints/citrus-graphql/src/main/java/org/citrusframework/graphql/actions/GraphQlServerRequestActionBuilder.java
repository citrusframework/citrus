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
package org.citrusframework.graphql.actions;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.util.StringUtils;
import org.citrusframework.graphql.validation.GraphQlMessageValidationContext;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;

/**
 * Receives a GraphQL request sent by the client under test and validates it. GET and POST requests
 * look the same to this action. Every HTTP request check stays available.
 */
public class GraphQlServerRequestActionBuilder extends HttpServerRequestActionBuilder
        implements org.citrusframework.api.actions.graphql.GraphQlServerRequestActionBuilder<ReceiveMessageAction, HttpServerRequestActionBuilder.HttpMessageBuilderSupport, HttpServerRequestActionBuilder> {

    private final GraphQlMessageValidationContext.Builder expectations = GraphQlMessageValidationContext.Builder.request();

    private GraphQlMessageValidationContext validationContext;

    @Override
    public GraphQlServerRequestActionBuilder operationName(String operationName) {
        expectations.operationName(operationName);
        return this;
    }

    @Override
    public GraphQlServerRequestActionBuilder query(String document) {
        expectations.query(document);
        return this;
    }

    @Override
    public GraphQlServerRequestActionBuilder variable(String name, Object expected) {
        expectations.variable(name, expected);
        return this;
    }

    @Override
    public GraphQlServerRequestActionBuilder variables(String json) {
        expectations.variables(json);
        return this;
    }

    @Override
    public GraphQlServerRequestActionBuilder strict(boolean strict) {
        expectations.strict(strict);
        return this;
    }

    @Override
    protected void reconcileValidationContexts() {
        super.reconcileValidationContexts();
        validationContext = GraphQlActions.reconcile(this, expectations, endpoint, endpointUri, hasControlBody(), validationContext);
    }

    private boolean hasControlBody() {
        return getMessagePayload().filter(StringUtils::hasText).isPresent() || getMessageResource().isPresent();
    }

    @Override
    public ReceiveMessageAction doBuild() {
        GraphQlActions.addProcessor(this, validationContext);
        return super.doBuild();
    }
}
