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

import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.graphql.message.GraphQlRequestMessageBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.spi.Resource;
import org.springframework.http.HttpMethod;

/**
 * Sends a GraphQL operation as client. Every HTTP request setting stays available; the GraphQL body,
 * path and method are written when the action runs.
 */
public class GraphQlClientRequestActionBuilder extends HttpClientRequestActionBuilder
        implements org.citrusframework.api.actions.graphql.GraphQlClientRequestActionBuilder<SendMessageAction, HttpClientRequestActionBuilder.HttpMessageBuilderSupport, HttpClientRequestActionBuilder> {

    private final GraphQlRequestMessageBuilder graphQlMessageBuilder;

    public GraphQlClientRequestActionBuilder() {
        this(new HttpMessage());
    }

    private GraphQlClientRequestActionBuilder(HttpMessage message) {
        this(new GraphQlRequestMessageBuilder(message), message);
    }

    private GraphQlClientRequestActionBuilder(GraphQlRequestMessageBuilder messageBuilder, HttpMessage message) {
        super(messageBuilder, message);
        this.graphQlMessageBuilder = messageBuilder;
    }

    @Override
    public GraphQlClientRequestActionBuilder query(String document) {
        graphQlMessageBuilder.setQuery(document);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder queryResource(String resourcePath) {
        graphQlMessageBuilder.setQueryResource(resourcePath);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder queryResource(Resource resource) {
        graphQlMessageBuilder.setQueryResource(resource);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder operationName(String operationName) {
        graphQlMessageBuilder.setOperationName(operationName);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder variable(String name, Object value) {
        graphQlMessageBuilder.addVariable(name, value);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder variables(String json) {
        graphQlMessageBuilder.setVariables(json);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder get() {
        graphQlMessageBuilder.setMethod(HttpMethod.GET);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder post() {
        graphQlMessageBuilder.setMethod(HttpMethod.POST);
        return this;
    }

    @Override
    public GraphQlClientRequestActionBuilder strict(boolean strict) {
        graphQlMessageBuilder.setStrict(strict);
        return this;
    }

    @Override
    public SendMessageAction doBuild() {
        graphQlMessageBuilder.setEndpoint(getEndpoint(), getEndpointUri());
        return super.doBuild();
    }
}
