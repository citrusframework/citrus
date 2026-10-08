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
package org.citrusframework.graphql.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.api.graphql.GraphQlError;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.graphql.actions.GraphQlActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientRequestActionBuilder;
import org.citrusframework.graphql.actions.GraphQlClientResponseActionBuilder;
import org.citrusframework.graphql.actions.GraphQlServerActionBuilder;
import org.citrusframework.graphql.actions.GraphQlServerRequestActionBuilder;
import org.citrusframework.graphql.actions.GraphQlServerResponseActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.actions.Message;
import org.citrusframework.xml.actions.Receive;
import org.citrusframework.xml.actions.Send;

/**
 * XML DSL of the GraphQL test actions: {@code <graphql client="...">} with {@code send-request} /
 * {@code receive-response}, {@code <graphql server="...">} with {@code receive-request} /
 * {@code send-response}. Generic message, validate and extract elements work as for send and receive.
 */
@XmlRootElement(name = "graphql")
public class GraphQl implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private final GraphQlActionBuilder actionBuilder = new GraphQlActionBuilder();
    private Object builder;

    private Send send;
    private Receive receive;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public void setDescription(String value) {
        this.description = value;
    }

    @XmlAttribute(name = "actor")
    public void setActor(String actor) {
        this.actor = actor;
    }

    @XmlAttribute(name = "client")
    public void setClient(String client) {
        builder = actionBuilder.client(client);
    }

    @XmlAttribute(name = "server")
    public void setServer(String server) {
        builder = actionBuilder.server(server);
    }

    @XmlElement(name = "send-request")
    public void setSendRequest(ClientRequest request) {
        GraphQlClientRequestActionBuilder requestBuilder = asClientBuilder().send();
        requestBuilder.description(description);

        if (request.query != null) {
            if (request.query.file != null) {
                requestBuilder.queryResource(request.query.file);
            } else {
                requestBuilder.query(request.query.value.trim());
            }
        }
        if (request.operationName != null) {
            requestBuilder.operationName(request.operationName);
        }
        if (request.variables != null) {
            requestBuilder.variables(request.variables.trim());
        }
        request.getVariableList().forEach(variable -> requestBuilder.variable(variable.name, variable.value));
        if ("GET".equalsIgnoreCase(request.method)) {
            requestBuilder.get();
        } else if ("POST".equalsIgnoreCase(request.method)) {
            requestBuilder.post();
        }
        if (request.strict != null) {
            requestBuilder.strict(request.strict);
        }

        send = new Send(requestBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // the actual build is called directly on the builder
                return null;
            }
        };
        if (request.message != null) {
            send.setMessage(request.message);
        }
        if (request.fork != null) {
            send.setFork(request.fork);
        }
        if (request.extract != null) {
            send.setExtract(request.extract);
        }
        if (request.uri != null) {
            requestBuilder.message().header(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, request.uri);
        }

        builder = requestBuilder;
    }

    @XmlElement(name = "receive-response")
    public void setReceiveResponse(ClientResponse response) {
        GraphQlClientResponseActionBuilder responseBuilder = asClientBuilder().receive();
        responseBuilder.description(description);

        response.getDataList().forEach(data -> {
            if (data.path != null) {
                responseBuilder.data(data.path, data.value);
            } else {
                responseBuilder.data(data.json.trim());
            }
        });
        if (response.expectErrors != null) {
            responseBuilder.expectErrors();
        }
        response.getExpectErrorList().forEach(error -> responseBuilder.expectError(error.toGraphQlError()));
        if (response.strict != null) {
            responseBuilder.strict(response.strict);
        }

        receive = receive(responseBuilder, response);
        if (response.status != null) {
            responseBuilder.message().status(response.status.intValue());
        }

        builder = responseBuilder;
    }

    @XmlElement(name = "receive-request")
    public void setReceiveRequest(ServerRequest request) {
        GraphQlServerRequestActionBuilder requestBuilder = asServerBuilder().receive();
        requestBuilder.description(description);

        if (request.operationName != null) {
            requestBuilder.operationName(request.operationName);
        }
        if (request.query != null) {
            requestBuilder.query(request.query.trim());
        }
        if (request.variables != null) {
            requestBuilder.variables(request.variables.trim());
        }
        request.getVariableList().forEach(variable -> requestBuilder.variable(variable.name, variable.value));
        if (request.strict != null) {
            requestBuilder.strict(request.strict);
        }

        receive = receive(requestBuilder, request);
        if (request.method != null) {
            requestBuilder.method(request.method);
        }

        builder = requestBuilder;
    }

    @XmlElement(name = "send-response")
    public void setSendResponse(ServerResponse response) {
        GraphQlServerResponseActionBuilder responseBuilder = asServerBuilder().send();
        responseBuilder.description(description);

        if (response.data != null) {
            responseBuilder.data(response.data.trim());
        }
        response.getErrorList().forEach(error -> responseBuilder.error(error.toGraphQlError()));
        if (response.strict != null) {
            responseBuilder.strict(response.strict);
        }

        send = new Send(responseBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // the actual build is called directly on the builder
                return null;
            }
        };
        if (response.message != null) {
            send.setMessage(response.message);
        }
        if (response.extract != null) {
            send.setExtract(response.extract);
        }
        if (response.status != null) {
            responseBuilder.message().status(response.status.intValue());
        }

        builder = responseBuilder;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing GraphQL client or server action - please provide proper action details");
        }

        if (send != null) {
            send.setReferenceResolver(referenceResolver);
            send.setActor(actor);
            send.build();
        }

        if (receive != null) {
            receive.setReferenceResolver(referenceResolver);
            receive.setActor(actor);
            receive.build();
        }

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        return ((TestActionBuilder<?>) builder).build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    private static Receive receive(ReceiveMessageAction.ReceiveMessageActionBuilder<?, ?, ?> receiveBuilder, ReceiveElement element) {
        Receive receive = new Receive(receiveBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // the actual build is called directly on the builder
                return null;
            }
        };

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
        element.getValidates().forEach(receive.getValidates()::add);
        if (element.extract != null) {
            receive.setExtract(element.extract);
        }

        return receive;
    }

    private GraphQlClientActionBuilder asClientBuilder() {
        if (builder instanceof GraphQlClientActionBuilder clientBuilder) {
            return clientBuilder;
        }

        throw new CitrusRuntimeException("Failed to convert '%s' to GraphQL client action builder - set the 'client' attribute"
                .formatted(Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    private GraphQlServerActionBuilder asServerBuilder() {
        if (builder instanceof GraphQlServerActionBuilder serverBuilder) {
            return serverBuilder;
        }

        throw new CitrusRuntimeException("Failed to convert '%s' to GraphQL server action builder - set the 'server' attribute"
                .formatted(Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Query {
        @XmlAttribute
        protected String file;
        @XmlValue
        protected String value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Variable {
        @XmlAttribute(required = true)
        protected String name;
        @XmlAttribute(required = true)
        protected String value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Data {
        @XmlAttribute
        protected String path;
        @XmlAttribute
        protected String value;
        @XmlValue
        protected String json;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Empty {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Error {
        @XmlAttribute
        protected String message;
        @XmlAttribute
        protected String path;
        @XmlAttribute
        protected String code;

        GraphQlError toGraphQlError() {
            GraphQlError error = GraphQlError.error().message(message).code(code);
            if (path != null) {
                error.path(GraphQlError.parsePath(path).toArray());
            }
            return error;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ClientRequest {
        @XmlAttribute
        protected String uri;
        @XmlAttribute
        protected Boolean fork;
        @XmlAttribute
        protected String method;
        @XmlAttribute
        protected Boolean strict;
        @XmlAttribute(name = "operation-name")
        protected String operationName;
        @XmlElement
        protected Query query;
        @XmlElement
        protected String variables;
        @XmlElement(name = "variable")
        protected List<Variable> variableList;
        @XmlElement
        protected Message message;
        @XmlElement
        protected Message.Extract extract;

        public List<Variable> getVariableList() {
            if (variableList == null) {
                variableList = new ArrayList<>();
            }
            return variableList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public abstract static class ReceiveElement {
        @XmlAttribute
        protected Integer timeout;
        @XmlAttribute
        protected Boolean strict;
        @XmlAttribute
        protected String select;
        @XmlAttribute
        protected String validator;
        @XmlAttribute
        protected String validators;
        @XmlAttribute(name = "header-validator")
        protected String headerValidator;
        @XmlAttribute(name = "header-validators")
        protected String headerValidators;
        @XmlElement
        protected Receive.Selector selector;
        @XmlElement
        protected Message message;
        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates;
        @XmlElement
        protected Message.Extract extract;

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }
            return validates;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ClientResponse extends ReceiveElement {
        @XmlAttribute
        protected Integer status;
        @XmlElement(name = "data")
        protected List<Data> dataList;
        @XmlElement(name = "expect-errors")
        protected Empty expectErrors;
        @XmlElement(name = "expect-error")
        protected List<Error> expectErrorList;

        public List<Data> getDataList() {
            if (dataList == null) {
                dataList = new ArrayList<>();
            }
            return dataList;
        }

        public List<Error> getExpectErrorList() {
            if (expectErrorList == null) {
                expectErrorList = new ArrayList<>();
            }
            return expectErrorList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ServerRequest extends ReceiveElement {
        @XmlAttribute
        protected String method;
        @XmlAttribute(name = "operation-name")
        protected String operationName;
        @XmlElement
        protected String query;
        @XmlElement
        protected String variables;
        @XmlElement(name = "variable")
        protected List<Variable> variableList;

        public List<Variable> getVariableList() {
            if (variableList == null) {
                variableList = new ArrayList<>();
            }
            return variableList;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ServerResponse {
        @XmlAttribute
        protected Integer status;
        @XmlAttribute
        protected Boolean strict;
        @XmlElement
        protected String data;
        @XmlElement(name = "error")
        protected List<Error> errorList;
        @XmlElement
        protected Message message;
        @XmlElement
        protected Message.Extract extract;

        public List<Error> getErrorList() {
            if (errorList == null) {
                errorList = new ArrayList<>();
            }
            return errorList;
        }
    }
}
