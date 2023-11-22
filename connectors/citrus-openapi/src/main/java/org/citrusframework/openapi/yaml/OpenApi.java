/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.openapi.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.actions.OpenApiActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;
import org.citrusframework.yaml.actions.Message;

/**
 * @author Christoph Deppisch
 */
public class OpenApi implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> builder;

    private Receive receive;
    private Send send;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = value;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setSpecification(String specification) {
        builder = new OpenApiActionBuilder().specification(specification);
    }

    public void setClient(String httpClient) {
        builder = ((OpenApiActionBuilder) builder).client(httpClient);
    }

    public void setServer(String httpServer) {
        builder = ((OpenApiActionBuilder) builder).server(httpServer);
    }

    public void setSendRequest(ClientRequest request) {
        OpenApiClientRequestActionBuilder requestBuilder =
                asClientBuilder().send(request.getOperation());

        requestBuilder.name("openapi:send-request");
        requestBuilder.description(description);

        send = new Send(requestBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

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

    public void setReceiveResponse(ClientResponse response) {
        OpenApiClientResponseActionBuilder responseBuilder =
                asClientBuilder().receive(response.getOperation(), response.getStatus());

        responseBuilder.name("openapi:receive-response");
        responseBuilder.description(description);

        receive = new Receive(responseBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.timeout != null) {
            receive.setTimeout(response.timeout);
        }

        receive.setSelect(response.select);
        receive.setValidator(response.validator);
        receive.setValidators(response.validators);
        receive.setHeaderValidator(response.headerValidator);
        receive.setHeaderValidators(response.headerValidators);

        if (response.selector != null) {
            receive.setSelector(response.selector);
        }

        receive.setSelect(response.select);

        response.getValidates().forEach(receive.getValidate()::add);

        if (response.extract != null) {
            receive.setExtract(response.extract);
        }

        builder = responseBuilder;
    }

    public void setReceiveRequest(ServerRequest request) {
        OpenApiServerRequestActionBuilder requestBuilder =
                asServerBuilder().receive(request.getOperation());

        requestBuilder.name("openapi:receive-request");
        requestBuilder.description(description);

        receive = new Receive(requestBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (request.selector != null) {
            receive.setSelector(request.selector);
        }

        receive.setSelect(request.select);
        receive.setValidator(request.validator);
        receive.setValidators(request.validators);
        receive.setHeaderValidator(request.headerValidator);
        receive.setHeaderValidators(request.headerValidators);

        if (request.timeout != null) {
            receive.setTimeout(request.timeout);
        }

        request.getValidates().forEach(receive.getValidate()::add);

        if (request.extract != null) {
            receive.setExtract(request.extract);
        }

        builder = requestBuilder;
    }

    public void setSendResponse(ServerResponse response) {
        HttpServerResponseActionBuilder responseBuilder =
                asServerBuilder().send(response.getOperation(), response.getStatus());

        responseBuilder.name("openapi:send-response");
        responseBuilder.description(description);

        send = new Send(responseBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.extract != null) {
            send.setExtract(response.extract);
        }

        responseBuilder.message().header(HttpMessageHeaders.HTTP_STATUS_CODE, response.getStatus());

        builder = responseBuilder;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing client or server Http action - please provide proper action details");
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

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Converts current builder to client builder.
     * @return
     */
    private OpenApiClientActionBuilder asClientBuilder() {
        if (builder instanceof OpenApiClientActionBuilder) {
            return (OpenApiClientActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to openapi client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    /**
     * Converts current builder to server builder.
     * @return
     */
    private OpenApiServerActionBuilder asServerBuilder() {
        if (builder instanceof OpenApiServerActionBuilder) {
            return (OpenApiServerActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to openapi server action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    public static class ClientRequest {
        protected String operation;
        protected String uri;
        protected Boolean fork;

        protected Message.Extract extract;

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getFork() {
            return fork;
        }

        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ServerRequest {
        protected Integer timeout;

        protected String operation;

        protected String select;

        protected String validator;

        protected String validators;

        protected String headerValidator;

        protected String headerValidators;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validates;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }

            return validates;
        }

        public void setValidates(List<Receive.Validate> validates) {
            this.validates = validates;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ServerResponse {
        protected String operation;

        protected String status = "200";

        protected Message.Extract extract;

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ClientResponse {
        protected Integer timeout;

        protected String operation;

        protected String status = "200";

        protected String select;

        protected String validator;

        protected String validators;

        protected String headerValidator;

        protected String headerValidators;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validates;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSelect() {
            return select;
        }

        public void setSelect(String select) {
            this.select = select;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        public String getValidator() {
            return validator;
        }

        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }

            return validates;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

}
