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
import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.graphql.message.GraphQlResponseMessageBuilder;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder;
import org.citrusframework.http.message.HttpMessage;

/**
 * Replies to the client under test with GraphQL {@code data} and/or {@code errors}. Every HTTP
 * response setting (status, headers, content type) stays available.
 */
public class GraphQlServerResponseActionBuilder extends HttpServerResponseActionBuilder
        implements org.citrusframework.api.actions.graphql.GraphQlServerResponseActionBuilder<SendMessageAction, HttpServerResponseActionBuilder.HttpMessageBuilderSupport, HttpServerResponseActionBuilder> {

    private final GraphQlResponseMessageBuilder graphQlMessageBuilder;

    public GraphQlServerResponseActionBuilder() {
        this(new HttpMessage());
    }

    private GraphQlServerResponseActionBuilder(HttpMessage message) {
        this(new GraphQlResponseMessageBuilder(message), message);
    }

    private GraphQlServerResponseActionBuilder(GraphQlResponseMessageBuilder messageBuilder, HttpMessage message) {
        super(messageBuilder, message);
        this.graphQlMessageBuilder = messageBuilder;
    }

    @Override
    public GraphQlServerResponseActionBuilder data(String json) {
        graphQlMessageBuilder.setData(json);
        return this;
    }

    @Override
    public GraphQlServerResponseActionBuilder error(GraphQlError error) {
        graphQlMessageBuilder.addError(error);
        return this;
    }

    @Override
    public GraphQlServerResponseActionBuilder strict(boolean strict) {
        graphQlMessageBuilder.setStrict(strict);
        return this;
    }

    @Override
    public SendMessageAction doBuild() {
        graphQlMessageBuilder.setEndpoint(getEndpoint(), getEndpointUri());
        return super.doBuild();
    }
}
