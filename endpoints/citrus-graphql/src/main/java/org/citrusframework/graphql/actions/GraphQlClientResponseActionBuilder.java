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
import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.graphql.validation.GraphQlMessageValidationContext;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;

/**
 * Receives a GraphQL response as client. Unexpected GraphQL errors fail the action; expected errors and
 * data are declared on this builder. Every HTTP response check stays available.
 */
public class GraphQlClientResponseActionBuilder extends HttpClientResponseActionBuilder
        implements org.citrusframework.api.actions.graphql.GraphQlClientResponseActionBuilder<ReceiveMessageAction, HttpClientResponseActionBuilder.HttpMessageBuilderSupport, HttpClientResponseActionBuilder> {

    private final GraphQlMessageValidationContext.Builder expectations = GraphQlMessageValidationContext.Builder.response();

    private GraphQlMessageValidationContext validationContext;

    @Override
    public GraphQlClientResponseActionBuilder data(String path, Object expected) {
        expectations.data(path, expected);
        return this;
    }

    @Override
    public GraphQlClientResponseActionBuilder data(String json) {
        expectations.data(json);
        return this;
    }

    @Override
    public GraphQlClientResponseActionBuilder expectErrors() {
        expectations.expectErrors();
        return this;
    }

    @Override
    public GraphQlClientResponseActionBuilder expectError(GraphQlError error) {
        expectations.expectError(error);
        return this;
    }

    @Override
    public GraphQlClientResponseActionBuilder strict(boolean strict) {
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
