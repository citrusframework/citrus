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

package org.citrusframework.openapi.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.ReceiveMessageAction.ReceiveMessageActionBuilder;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerResponseActionBuilder;
import org.citrusframework.openapi.actions.OpenApiSpecificationSourceAwareBuilder;
import org.citrusframework.openapi.validation.OpenApiMessageValidationContext;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

import static org.citrusframework.openapi.OpenApiSettings.getOpenApiValidationPolicy;
import static org.citrusframework.openapi.OpenApiSettings.getRequestAutoFillRandomValues;
import static org.citrusframework.openapi.OpenApiSettings.getResponseAutoFillRandomValues;
import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class OpenApi implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final String OPENAPI_GROUP = "openapi";

    private OpenApiSpecificationSourceAwareBuilder<?> builder;

    private Receive receive;
    private Send send;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    @SchemaProperty(required = true, description = "The OpenAPI specification source. Can be a local file resource or an Http endpoint URI.")
    public void setSpecification(String specification) {
        if (builder == null) {
            builder = new OpenApiActionBuilder().specification(specification);
        } else {
            builder.getOpenApiSpecificationSource().setOpenApiSpecification(
                    OpenApiSpecification.from(specification, getOpenApiValidationPolicy()));
        }
    }

    @SchemaProperty(description = "Sets the Http client used to send requests.")
    public void setClient(String httpClient) {
        if (builder == null) {
            builder = new OpenApiActionBuilder().client(httpClient);
        } else if (builder instanceof OpenApiActionBuilder openApiActionBuilder) {
            builder = openApiActionBuilder.client(httpClient);
        } else if (builder instanceof OpenApiClientActionBuilder openApiClientActionBuilder) {
            openApiClientActionBuilder.client(httpClient);
        }  else if (builder instanceof SendActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpClient);
        } else if (builder instanceof ReceiveActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpClient);
        }
    }

    @SchemaProperty(description = "Sets the Http server that provides the Http API.")
    public void setServer(String httpServer) {
        if (builder == null) {
            builder = new OpenApiActionBuilder().server(httpServer);
        } else if (builder instanceof OpenApiActionBuilder openApiActionBuilder) {
            builder = openApiActionBuilder.server(httpServer);
        } else if (builder instanceof OpenApiServerActionBuilder openApiServerActionBuilder) {
            openApiServerActionBuilder.server(httpServer);
        } else if (builder instanceof SendActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpServer);
        } else if (builder instanceof ReceiveActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpServer);
        }
    }

    @SchemaProperty(kind = ACTION, group = OPENAPI_GROUP, description = "Send a request as a client.")
    public void setSendRequest(ClientRequest request) {
        OpenApiClientRequestActionBuilder requestBuilder =
            asClientBuilder().send(request.getOperation());

        requestBuilder.name("openapi:send-request");
        requestBuilder.description(description);

        if (request.getSchemaValidation() != null) {
            requestBuilder.schemaValidation(request.getSchemaValidation());
        }

        if (request.getAutoFill() != null) {
            requestBuilder.autoFill(request.getAutoFill());
        }

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

    @SchemaProperty(kind = ACTION, group = OPENAPI_GROUP, description = "Receives a response as a client.")
    public void setReceiveResponse(ClientResponse response) {
        OpenApiClientResponseActionBuilder responseBuilder =
                asClientBuilder().receive(response.getOperation(), response.getStatus());

        responseBuilder.name("openapi:receive-response");
        responseBuilder.description(description);

        if (response.getSchemaValidation() != null) {
            responseBuilder.schemaValidation(response.getSchemaValidation());
        }

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

    @SchemaProperty(kind = ACTION, group = OPENAPI_GROUP, description = "Receives a client request as a server.")
    public void setReceiveRequest(ServerRequest request) {
        OpenApiServerRequestActionBuilder requestBuilder =
                asServerBuilder().receive(request.getOperation());

        requestBuilder.name("openapi:receive-request");
        requestBuilder.description(description);

        if (request.getSchemaValidation() != null) {
            requestBuilder.schemaValidation(request.getSchemaValidation());
        }

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

    @SchemaProperty(kind = ACTION, group = OPENAPI_GROUP, description = "Sends a response as a server.")
    public void setSendResponse(ServerResponse response) {
        OpenApiServerResponseActionBuilder responseBuilder =
                asServerBuilder().send(response.getOperation(), response.getStatus());

        responseBuilder.name("openapi:send-response");
        responseBuilder.description(description);

        if (response.getSchemaValidation() != null) {
            responseBuilder.schemaValidation(response.getSchemaValidation());
        }

        if (response.getAutoFill() != null) {
            responseBuilder.autoFill(response.getAutoFill());
        }

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

        if (builder instanceof ReceiveMessageActionBuilder<?,?,?> receiveMessageActionBuilder) {
            receiveMessageActionBuilder.validate(OpenApiMessageValidationContext.Builder.openApi(builder.getOpenApiSpecificationSource().resolve(referenceResolver)));
        }
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    /**
     * Converts current builder to client builder.
     */
    private OpenApiClientActionBuilder asClientBuilder() {
        if (builder == null) {
            builder = new OpenApiActionBuilder().client();
        }

        if (builder instanceof OpenApiClientActionBuilder clientBuilder) {
            return clientBuilder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to openapi client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    /**
     * Converts current builder to server builder.
     */
    private OpenApiServerActionBuilder asServerBuilder() {
        if (builder == null) {
            builder = new OpenApiActionBuilder().server();
        }
        if (builder instanceof OpenApiServerActionBuilder serverBuilder) {
            return serverBuilder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to openapi server action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    public static class ClientRequest {
        protected String operation;
        protected String uri;
        protected Boolean fork;
        protected Boolean schemaValidation;
        protected AutoFillType autoFill = getRequestAutoFillRandomValues();

        protected Message.Extract extract;

        public String getOperation() {
            return operation;
        }

        @SchemaProperty(required = true, description = "The API operation to call.")
        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getUri() {
            return uri;
        }

        @SchemaProperty(advanced = true, description = "Http endpoint URI overwrite.")
        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getFork() {
            return fork;
        }

        @SchemaProperty(advanced = true, description = "When enabled send operation does not block while waiting for the response.")
        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        public Boolean getSchemaValidation() {
            return schemaValidation;
        }

        @SchemaProperty(advanced = true, description = "Enables the schema validation.", defaultValue = "false")
        public void setSchemaValidation(Boolean schemaValidation) {
            this.schemaValidation = schemaValidation;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables before the request is sent.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        public AutoFillType getAutoFill() {
            return autoFill;
        }

        @SchemaProperty(description = "Define which of the request fields are automatically filled with generated test data.")
        public void setAutoFill(AutoFillType autoFill) {
            this.autoFill = autoFill;
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

        protected Boolean schemaValidation;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validates;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        @SchemaProperty(description = "The timeout while waiting for incoming Http requests.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getOperation() {
            return operation;
        }

        @SchemaProperty(required = true, description = "The expected API operation.")
        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getSelect() {
            return select;
        }

        @SchemaProperty(advanced = true, description = "Message selector expression to selectively consume messages.")
        public void setSelect(String select) {
            this.select = select;
        }

        public String getValidator() {
            return validator;
        }

        @SchemaProperty(advanced = true, description = "Explicit message validator.")
        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        @SchemaProperty(advanced = true, description = "List of message validators used to validate the message.")
        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        @SchemaProperty(advanced = true, description = "Explicit message header validator.")
        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        @SchemaProperty(advanced = true, description = "List of message header validators used to validate the message.")
        public void setHeaderValidators(String headerValidators) {
            this.headerValidators = headerValidators;
        }

        public List<Receive.Validate> getValidates() {
            if (validates == null) {
                validates = new ArrayList<>();
            }

            return validates;
        }

        @SchemaProperty(description = "Additional message validation expressions.")
        public void setValidates(List<Receive.Validate> validates) {
            this.validates = validates;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        public Boolean getSchemaValidation() {
            return schemaValidation;
        }

        @SchemaProperty(description = "Enables the schema validation.", defaultValue = "false")
        public void setSchemaValidation(Boolean schemaValidation) {
            this.schemaValidation = schemaValidation;
        }
    }

    public static class ServerResponse {
        protected String operation;

        protected String status = "200";

        protected Boolean schemaValidation;

        protected AutoFillType autoFill = getResponseAutoFillRandomValues();

        protected Message.Extract extract;

        public String getOperation() {
            return operation;
        }

        @SchemaProperty(required = true, description = "The OpenAPI operation that generates the response.")
        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getStatus() {
            return status;
        }

        @SchemaProperty(description = "The response to generate.")
        public void setStatus(String status) {
            this.status = status;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        public Boolean getSchemaValidation() {
            return schemaValidation;
        }

        @SchemaProperty(advanced = true, description = "Enables the schema validation.", defaultValue = "false")
        public void setSchemaValidation(Boolean schemaValidation) {
            this.schemaValidation = schemaValidation;
        }

        public AutoFillType getAutoFill() {
            return autoFill;
        }

        @SchemaProperty(description = "Define which response fields are set with generated values.")
        public void setAutoFill(AutoFillType autoFill) {
            this.autoFill = autoFill;
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

        protected Boolean schemaValidation;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validates;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        @SchemaProperty(description = "Timeout in milliseconds while waiting for the server response.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getOperation() {
            return operation;
        }

        @SchemaProperty(required = true, description = "The OpenAPI operation that defines the expected response.")
        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getStatus() {
            return status;
        }

        @SchemaProperty(description = "Expected response status.")
        public void setStatus(String status) {
            this.status = status;
        }

        public String getSelect() {
            return select;
        }

        @SchemaProperty(advanced = true, description = "Message selector expression to selectively consume messages.")
        public void setSelect(String select) {
            this.select = select;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        @SchemaProperty(advanced = true, description = "Message selector to selectively consume messages.")
        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        public String getValidator() {
            return validator;
        }

        @SchemaProperty(advanced = true, description = "Explicit message validator.")
        public void setValidator(String validator) {
            this.validator = validator;
        }

        public String getValidators() {
            return validators;
        }

        @SchemaProperty(advanced = true, description = "List of message validators used to validate the message.")
        public void setValidators(String validators) {
            this.validators = validators;
        }

        public String getHeaderValidator() {
            return headerValidator;
        }

        @SchemaProperty(advanced = true, description = "Explicit message header validator.")
        public void setHeaderValidator(String headerValidator) {
            this.headerValidator = headerValidator;
        }

        public String getHeaderValidators() {
            return headerValidators;
        }

        @SchemaProperty(advanced = true, description = "List of message header validators used to validate the message.")
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

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }

        public Boolean getSchemaValidation() {
            return schemaValidation;
        }

        @SchemaProperty(description = "Enables the schema validation.", defaultValue = "false")
        public void setSchemaValidation(Boolean schemaValidation) {
            this.schemaValidation = schemaValidation;
        }

    }
}
