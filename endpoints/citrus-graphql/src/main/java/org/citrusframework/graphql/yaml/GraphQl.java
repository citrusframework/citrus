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
package org.citrusframework.graphql.yaml;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.actions.GraphQlActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientRequestActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientResponseActionBuilder;
import org.citrusframework.graphql.actions.GraphQlServerRequestActionBuilder;
import org.citrusframework.graphql.actions.GraphQlServerResponseActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

import static org.citrusframework.api.yaml.SchemaProperty.Kind.ACTION;

/**
 * YAML DSL of the GraphQL test actions. Properties may come in any order; the actions are built once
 * the client or server is known.
 */
public class GraphQl implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final String GRAPHQL_GROUP = "graphql";
    private static final String GRAPHQL_MODULE = "citrus-graphql";

    private String client;
    private String server;
    private String description;
    private String actor;

    private ClientRequest sendRequest;
    private ClientResponse receiveResponse;
    private ServerRequest receiveRequest;
    private ServerResponse sendResponse;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    @SchemaProperty(description = "The GraphQL client calling the GraphQL API. Uses an endpoint URI or references an endpoint name.")
    public void setClient(String client) {
        this.client = client;
    }

    @SchemaProperty(description = "The GraphQL server simulating the GraphQL API. References an endpoint name.")
    public void setServer(String server) {
        this.server = server;
    }

    @SchemaProperty(kind = ACTION, group = GRAPHQL_GROUP, module = GRAPHQL_MODULE,
            description = "Sends a GraphQL operation as client.")
    public void setSendRequest(ClientRequest request) {
        this.sendRequest = request;
    }

    @SchemaProperty(kind = ACTION, group = GRAPHQL_GROUP, module = GRAPHQL_MODULE,
            description = "Receives a GraphQL response as client.")
    public void setReceiveResponse(ClientResponse response) {
        this.receiveResponse = response;
    }

    @SchemaProperty(kind = ACTION, group = GRAPHQL_GROUP, module = GRAPHQL_MODULE,
            description = "Receives a GraphQL operation as server.")
    public void setReceiveRequest(ServerRequest request) {
        this.receiveRequest = request;
    }

    @SchemaProperty(kind = ACTION, group = GRAPHQL_GROUP, module = GRAPHQL_MODULE,
            description = "Replies with GraphQL data and errors as server.")
    public void setSendResponse(ServerResponse response) {
        this.sendResponse = response;
    }

    @Override
    public TestAction build() {
        GraphQlActionBuilder actions = new GraphQlActionBuilder().withReferenceResolver(referenceResolver);

        if (sendRequest != null) {
            return buildSendRequest(actions.client(requireClient()).send());
        }
        if (receiveResponse != null) {
            return buildReceiveResponse(actions.client(requireClient()).receive());
        }
        if (receiveRequest != null) {
            return buildReceiveRequest(actions.server(requireServer()).receive());
        }
        if (sendResponse != null) {
            return buildSendResponse(actions.server(requireServer()).send());
        }

        throw new CitrusRuntimeException("Missing GraphQL action - set one of sendRequest, receiveResponse, receiveRequest or sendResponse");
    }

    private TestAction buildSendRequest(GraphQlClientRequestActionBuilder builder) {
        builder.description(description);
        ClientRequest request = sendRequest;
        if (request.queryResource != null) {
            builder.queryResource(request.queryResource);
        } else if (request.query != null) {
            builder.query(request.query);
        }
        if (request.operationName != null) {
            builder.operationName(request.operationName);
        }
        if (request.variables != null) {
            builder.variables(request.variables);
        }
        request.variable.forEach(variable -> builder.variable(variable.name, variable.value));
        if ("GET".equalsIgnoreCase(request.method)) {
            builder.get();
        } else if ("POST".equalsIgnoreCase(request.method)) {
            builder.post();
        }
        if (request.strict != null) {
            builder.strict(request.strict);
        }
        if (request.uri != null) {
            builder.message().header(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, request.uri);
        }

        Send send = send(builder);
        if (request.message != null) {
            send.setMessage(request.message);
        }
        if (request.fork != null) {
            send.setFork(request.fork);
        }
        if (request.extract != null) {
            send.setExtract(request.extract);
        }
        send.build();

        return builder.build();
    }

    private TestAction buildReceiveResponse(GraphQlClientResponseActionBuilder builder) {
        builder.description(description);
        ClientResponse response = receiveResponse;
        response.data.forEach(data -> {
            if (data.path != null) {
                builder.data(data.path, data.value);
            } else {
                builder.data(data.json);
            }
        });
        if (Boolean.TRUE.equals(response.expectErrors)) {
            builder.expectErrors();
        }
        response.expectError.forEach(error -> builder.expectError(error.toGraphQlError()));
        if (response.strict != null) {
            builder.strict(response.strict);
        }

        receive(builder, response).build();
        if (response.status != null) {
            builder.message().status(response.status.intValue());
        }

        return builder.build();
    }

    private TestAction buildReceiveRequest(GraphQlServerRequestActionBuilder builder) {
        builder.description(description);
        ServerRequest request = receiveRequest;
        if (request.operationName != null) {
            builder.operationName(request.operationName);
        }
        if (request.query != null) {
            builder.query(request.query);
        }
        if (request.variables != null) {
            builder.variables(request.variables);
        }
        request.variable.forEach(variable -> builder.variable(variable.name, variable.value));
        if (request.strict != null) {
            builder.strict(request.strict);
        }

        receive(builder, request).build();
        if (request.method != null) {
            builder.method(request.method);
        }

        return builder.build();
    }

    private TestAction buildSendResponse(GraphQlServerResponseActionBuilder builder) {
        builder.description(description);
        ServerResponse response = sendResponse;
        if (response.data != null) {
            builder.data(response.data);
        }
        response.error.forEach(error -> builder.error(error.toGraphQlError()));
        if (response.strict != null) {
            builder.strict(response.strict);
        }

        Send send = send(builder);
        if (response.message != null) {
            send.setMessage(response.message);
        }
        if (response.extract != null) {
            send.setExtract(response.extract);
        }
        send.build();
        if (response.status != null) {
            builder.message().status(response.status.intValue());
        }

        return builder.build();
    }

    private Send send(SendMessageAction.SendMessageActionBuilder<?, ?, ?> builder) {
        Send send = new Send(builder) {
            @Override
            protected SendMessageAction doBuild() {
                // the actual build is called directly on the builder
                return null;
            }
        };
        send.setReferenceResolver(referenceResolver);
        send.setActor(actor);
        return send;
    }

    private Receive receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> builder, ReceiveElement element) {
        Receive receive = new Receive(builder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // the actual build is called directly on the builder
                return null;
            }
        };
        receive.setReferenceResolver(referenceResolver);
        receive.setActor(actor);

        if (element.message != null) {
            receive.setMessage(element.message);
        }
        if (element.timeout != null) {
            receive.setTimeout(element.timeout);
        }
        if (element.selector != null) {
            receive.setSelector(element.selector);
        }
        receive.setSelect(element.select);
        receive.setValidator(element.validator);
        receive.setValidators(element.validators);
        receive.setHeaderValidator(element.headerValidator);
        receive.setHeaderValidators(element.headerValidators);
        element.validate.forEach(receive.getValidate()::add);
        if (element.extract != null) {
            receive.setExtract(element.extract);
        }

        return receive;
    }

    private String requireClient() {
        if (client == null) {
            throw new CitrusRuntimeException("Missing GraphQL client - set the 'client' property");
        }
        return client;
    }

    private String requireServer() {
        if (server == null) {
            throw new CitrusRuntimeException("Missing GraphQL server - set the 'server' property");
        }
        return server;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class Variable {
        protected String name;
        protected String value;

        @SchemaProperty(required = true, description = "The variable name.")
        public void setName(String name) {
            this.name = name;
        }

        @SchemaProperty(required = true, description = "The variable value; sent as string. Use 'variables' for typed values.")
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Data {
        protected String path;
        protected String value;
        protected String json;

        @SchemaProperty(description = "JSONPath relative to $.data.")
        public void setPath(String path) {
            this.path = path;
        }

        @SchemaProperty(description = "Expected value at the path; validation matchers are supported.")
        public void setValue(String value) {
            this.value = value;
        }

        @SchemaProperty(description = "Expected data object as JSON, compared as a whole.")
        public void setJson(String json) {
            this.json = json;
        }
    }

    public static class Error {
        protected String message;
        protected String path;
        protected String code;

        @SchemaProperty(description = "The error message; validation matchers are supported when expecting errors.")
        public void setMessage(String message) {
            this.message = message;
        }

        @SchemaProperty(description = "The error path, e.g. books[1].title.")
        public void setPath(String path) {
            this.path = path;
        }

        @SchemaProperty(description = "The error code in extensions.code.")
        public void setCode(String code) {
            this.code = code;
        }

        GraphQlError toGraphQlError() {
            GraphQlError error = GraphQlError.error().message(message).code(code);
            if (path != null) {
                error.path(GraphQlError.parsePath(path).toArray());
            }
            return error;
        }
    }

    public static class ClientRequest {
        protected String query;
        protected String queryResource;
        protected String operationName;
        protected String variables;
        protected List<Variable> variable = new ArrayList<>();
        protected String method;
        protected Boolean strict;
        protected String uri;
        protected Boolean fork;
        protected Message message;
        protected Message.Extract extract;

        @SchemaProperty(description = "The GraphQL document.")
        public void setQuery(String query) {
            this.query = query;
        }

        @SchemaProperty(description = "Resource path of the GraphQL document.")
        public void setQueryResource(String queryResource) {
            this.queryResource = queryResource;
        }

        @SchemaProperty(description = "The operation to execute.")
        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        @SchemaProperty(description = "The variables as JSON object; Citrus expressions are resolved before parsing.")
        public void setVariables(String variables) {
            this.variables = variables;
        }

        @SchemaProperty(description = "Single variables.")
        public void setVariable(List<Variable> variable) {
            this.variable = variable;
        }

        @SchemaProperty(description = "The request method, POST (default) or GET.")
        public void setMethod(String method) {
            this.method = method;
        }

        @SchemaProperty(description = "Enables or disables the GraphQL document checks.", defaultValue = "true")
        public void setStrict(Boolean strict) {
            this.strict = strict;
        }

        @SchemaProperty(advanced = true, description = "Endpoint URI overwrite.")
        public void setUri(String uri) {
            this.uri = uri;
        }

        @SchemaProperty(advanced = true, description = "When enabled the send operation does not block while waiting for the response.")
        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        @SchemaProperty(advanced = true, description = "Additional message settings such as headers.")
        public void setMessage(Message message) {
            this.message = message;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables before the request is sent.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public abstract static class ReceiveElement {
        protected Integer timeout;
        protected Boolean strict;
        protected String select;
        protected String validator;
        protected String validators;
        protected String headerValidator;
        protected String headerValidators;
        protected Receive.Selector selector;
        protected Message message;
        protected List<Receive.Validate> validate = new ArrayList<>();
        protected Message.Extract extract;

        @SchemaProperty(advanced = true, description = "Receive timeout in milliseconds.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        @SchemaProperty(description = "Enables or disables the GraphQL checks.", defaultValue = "true")
        public void setStrict(Boolean strict) {
            this.strict = strict;
        }

        @SchemaProperty(advanced = true, description = "Message selector expression.")
        public void setSelect(String select) {
            this.select = select;
        }

        @SchemaProperty(advanced = true, description = "Explicit message validator reference.")
        public void setValidator(String validator) {
            this.validator = validator;
        }

        @SchemaProperty(advanced = true, description = "Explicit message validator references.")
        public void setValidators(String validators) {
            this.validators = validators;
        }

        @SchemaProperty(advanced = true, description = "Explicit header validator reference.")
        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        @SchemaProperty(advanced = true, description = "Explicit header validator references.")
        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        @SchemaProperty(advanced = true, description = "Message selector.")
        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        @SchemaProperty(advanced = true, description = "Expected message headers or body.")
        public void setMessage(Message message) {
            this.message = message;
        }

        @SchemaProperty(advanced = true, description = "Additional message validations, e.g. JSONPath expressions.")
        public void setValidate(List<Receive.Validate> validate) {
            this.validate = validate;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ClientResponse extends ReceiveElement {
        protected Integer status;
        protected List<Data> data = new ArrayList<>();
        protected Boolean expectErrors;
        protected List<Error> expectError = new ArrayList<>();

        @SchemaProperty(description = "Expected HTTP status code.")
        public void setStatus(Integer status) {
            this.status = status;
        }

        @SchemaProperty(description = "Expected response data.")
        public void setData(List<Data> data) {
            this.data = data;
        }

        @SchemaProperty(description = "Expects at least one GraphQL error.")
        public void setExpectErrors(Boolean expectErrors) {
            this.expectErrors = expectErrors;
        }

        @SchemaProperty(description = "Expected GraphQL errors; each must match an error of the response.")
        public void setExpectError(List<Error> expectError) {
            this.expectError = expectError;
        }
    }

    public static class ServerRequest extends ReceiveElement {
        protected String method;
        protected String operationName;
        protected String query;
        protected String variables;
        protected List<Variable> variable = new ArrayList<>();

        @SchemaProperty(description = "Expected request method, POST or GET.")
        public void setMethod(String method) {
            this.method = method;
        }

        @SchemaProperty(description = "Expected operation name.")
        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        @SchemaProperty(description = "Expected GraphQL document.")
        public void setQuery(String query) {
            this.query = query;
        }

        @SchemaProperty(description = "Expected variables as JSON object.")
        public void setVariables(String variables) {
            this.variables = variables;
        }

        @SchemaProperty(description = "Expected single variables; validation matchers are supported.")
        public void setVariable(List<Variable> variable) {
            this.variable = variable;
        }
    }

    public static class ServerResponse {
        protected Integer status;
        protected Boolean strict;
        protected String data;
        protected List<Error> error = new ArrayList<>();
        protected Message message;
        protected Message.Extract extract;

        @SchemaProperty(description = "HTTP status code of the reply.")
        public void setStatus(Integer status) {
            this.status = status;
        }

        @SchemaProperty(description = "Enables or disables the GraphQL reply check.", defaultValue = "true")
        public void setStrict(Boolean strict) {
            this.strict = strict;
        }

        @SchemaProperty(description = "Reply data as JSON.")
        public void setData(String data) {
            this.data = data;
        }

        @SchemaProperty(description = "Reply errors.")
        public void setError(List<Error> error) {
            this.error = error;
        }

        @SchemaProperty(advanced = true, description = "Additional message settings such as headers or a raw body.")
        public void setMessage(Message message) {
            this.message = message;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }
}
