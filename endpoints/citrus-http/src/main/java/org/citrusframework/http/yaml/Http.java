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

package org.citrusframework.http.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpActionBuilder;
import org.citrusframework.http.actions.HttpClientActionBuilder;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.actions.HttpServerActionBuilder;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.SchemaType;
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;

public class Http implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final String HTTP_GROUP = "http";

    private TestActionBuilder<?> builder;

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

    @SchemaProperty(description = "The Http client.")
    public void setClient(String httpClient) {
        if (builder == null) {
            builder = new HttpActionBuilder().client(httpClient);
        } else if (builder instanceof SendActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpClient);
        } else if (builder instanceof ReceiveActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpClient);
        }
    }

    @SchemaProperty(description = "The Http server.")
    public void setServer(String httpServer) {
        if (builder == null) {
            builder = new HttpActionBuilder().server(httpServer);
        } else if (builder instanceof SendActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpServer);
        } else if (builder instanceof ReceiveActionBuilder<?,?,?> messageActionBuilder) {
            messageActionBuilder.endpoint(httpServer);
        }
    }

    @SchemaProperty(kind = ACTION, group = HTTP_GROUP, description = "Send a Http request as a client.")
    public void setSendRequest(ClientRequest request) {
        HttpClientRequestActionBuilder requestBuilder;
        HttpRequest requestMessage;
        if (request.getGET() != null) {
            requestMessage = request.getGET();
            requestBuilder = asClientBuilder().send().get(requestMessage.path);
        } else if (request.getPOST() != null) {
            requestMessage = request.getPOST();
            requestBuilder = asClientBuilder().send().post(requestMessage.path);
        } else if (request.getPUT() != null) {
            requestMessage = request.getPUT();
            requestBuilder = asClientBuilder().send().put(requestMessage.path);
        } else if (request.getDELETE() != null) {
            requestMessage = request.getDELETE();
            requestBuilder = asClientBuilder().send().delete(requestMessage.path);
        } else if (request.getHEAD() != null) {
            requestMessage = request.getHEAD();
            requestBuilder = asClientBuilder().send().head(requestMessage.path);
        } else if (request.getOPTIONS() != null) {
            requestMessage = request.getOPTIONS();
            requestBuilder = asClientBuilder().send().options(requestMessage.path);
        } else if (request.getPATCH() != null) {
            requestMessage = request.getPATCH();
            requestBuilder = asClientBuilder().send().patch(requestMessage.path);
        } else if (request.getTRACE() != null) {
            requestMessage = request.getTRACE();
            requestBuilder = asClientBuilder().send().trace(requestMessage.path);
        } else {
            throw new CitrusRuntimeException("Failed to construct proper Http client request action - missing proper Http method (GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH, TRACE)");
        }

        requestBuilder.name("http:send-request");
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

        send.setMessage(requestMessage);

        if (request.extract != null) {
            send.setExtract(request.extract);
        }

        if (request.uri != null) {
            requestBuilder.message().header(EndpointUriResolver.ENDPOINT_URI_HEADER_NAME, request.uri);
        }

        if (requestMessage.contentType != null) {
            requestBuilder.message().contentType(requestMessage.contentType);
        }

        if (requestMessage.accept != null) {
            requestBuilder.message().accept(requestMessage.accept);
        }

        if (requestMessage.version != null) {
            requestBuilder.message().version(requestMessage.version);
        }

        for (HttpRequest.QueryParameter param : requestMessage.getParameters()) {
            if (param.value != null) {
                requestBuilder.queryParam(param.name, param.value);
            } else {
                requestBuilder.queryParam(param.name);
            }
        }

        builder = requestBuilder;
    }

    @SchemaProperty(kind = ACTION, group = HTTP_GROUP, description = "Receive an Http response as a client.")
    public void setReceiveResponse(ClientResponse response) {
        HttpClientResponseActionBuilder responseBuilder = asClientBuilder().receive().response().name("http:receive-response");

        responseBuilder.description(description);

        receive = new Receive(responseBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.getResponse() !=  null) {
            responseBuilder.message().header(HttpMessageHeaders.HTTP_STATUS_CODE, response.getResponse().status);

            if (response.getResponse().reasonPhrase != null) {
                responseBuilder.message().reasonPhrase(response.getResponse().reasonPhrase);
            }

            if (response.getResponse().contentType != null) {
                responseBuilder.message().contentType(response.getResponse().contentType);
            }

            if (response.getResponse().version != null) {
                responseBuilder.message().version(response.getResponse().version);
            }

            receive.setMessage(response.getResponse());

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

            response.getValidate().forEach(receive.getValidate()::add);

            if (response.extract != null) {
                receive.setExtract(response.extract);
            }
        }

        builder = responseBuilder;
    }

    @SchemaProperty(kind = ACTION, group = HTTP_GROUP, description = "Receive an Http request as a server.")
    public void setReceiveRequest(ServerRequest request) {
        HttpServerRequestActionBuilder requestBuilder;
        HttpRequest requestMessage;
        if (request.getGET() != null) {
            requestMessage = request.getGET();
            requestBuilder = asServerBuilder().receive().get(requestMessage.path);
        } else if (request.getPOST() != null) {
            requestMessage = request.getPOST();
            requestBuilder = asServerBuilder().receive().post(requestMessage.path);
        } else if (request.getPUT() != null) {
            requestMessage = request.getPUT();
            requestBuilder = asServerBuilder().receive().put(requestMessage.path);
        } else if (request.getDELETE() != null) {
            requestMessage = request.getDELETE();
            requestBuilder = asServerBuilder().receive().delete(requestMessage.path);
        } else if (request.getHEAD() != null) {
            requestMessage = request.getHEAD();
            requestBuilder = asServerBuilder().receive().head(requestMessage.path);
        } else if (request.getOPTIONS() != null) {
            requestMessage = request.getOPTIONS();
            requestBuilder = asServerBuilder().receive().options(requestMessage.path);
        } else if (request.getPATCH() != null) {
            requestMessage = request.getPATCH();
            requestBuilder = asServerBuilder().receive().patch(requestMessage.path);
        } else if (request.getTRACE() != null) {
            requestMessage = request.getTRACE();
            requestBuilder = asServerBuilder().receive().trace(requestMessage.path);
        } else {
            throw new CitrusRuntimeException("Failed to construct proper Http client request action - missing proper Http method (GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH, TRACE)");
        }

        requestBuilder.name("http:receive-request");
        requestBuilder.description(description);

        if (requestMessage.contentType != null) {
            requestBuilder.message().contentType(requestMessage.contentType);
        }

        if (requestMessage.accept != null) {
            requestBuilder.message().accept(requestMessage.accept);
        }

        if (requestMessage.version != null) {
            requestBuilder.message().version(requestMessage.version);
        }

        receive = new Receive(requestBuilder) {
            @Override
            protected ReceiveMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        receive.setMessage(requestMessage);

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

        request.getValidate().forEach(receive.getValidate()::add);

        if (request.extract != null) {
            receive.setExtract(request.extract);
        }

        for (HttpRequest.QueryParameter param : requestMessage.getParameters()) {
            if (param.value != null) {
                requestBuilder.queryParam(param.name, param.value);
            } else {
                requestBuilder.queryParam(param.name);
            }
        }

        builder = requestBuilder;
    }

    @SchemaProperty(kind = ACTION, group = HTTP_GROUP, description = "Send an Http response as a server.")
    public void setSendResponse(ServerResponse response) {
        HttpServerResponseActionBuilder responseBuilder = asServerBuilder().send().response().name("http:send-response");

        responseBuilder.description(description);

        send = new Send(responseBuilder) {
            @Override
            protected SendMessageAction doBuild() {
                // do not build inside delegate. the actual build is called directly on the builder.
                return null;
            }
        };

        if (response.getResponse() !=  null) {
            send.setMessage(response.getResponse());

            if (response.extract != null) {
                send.setExtract(response.extract);
            }

            responseBuilder.message().header(HttpMessageHeaders.HTTP_STATUS_CODE, response.getResponse().status);

            if (response.getResponse().reasonPhrase != null) {
                responseBuilder.message().reasonPhrase(response.getResponse().reasonPhrase);
            }

            if (response.getResponse().contentType != null) {
                responseBuilder.message().contentType(response.getResponse().contentType);
            }

            if (response.getResponse().version != null) {
                responseBuilder.message().version(response.getResponse().version);
            }
        }

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
     */
    private HttpClientActionBuilder asClientBuilder() {
        if (builder == null) {
            builder = new HttpActionBuilder().client();
        }

        if (builder instanceof HttpClientActionBuilder) {
            return (HttpClientActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to http client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    /**
     * Converts current builder to server builder.
     */
    private HttpServerActionBuilder asServerBuilder() {
        if (builder == null) {
            builder = new HttpActionBuilder().server();
        }

        if (builder instanceof HttpServerActionBuilder) {
            return (HttpServerActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to http server action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    @SchemaType(oneOf = {
       "GET",
       "POST",
       "PUT",
       "PUT",
       "DELETE",
       "HEAD",
       "OPTIONS",
       "PATCH",
       "TRACE"
    })
    public static class ClientRequest {
        protected String uri;
        protected Boolean fork;

        protected HttpRequest get;
        protected HttpRequest post;
        protected HttpRequest put;
        protected HttpRequest delete;
        protected HttpRequest head;
        protected HttpRequest options;
        protected HttpRequest patch;
        protected HttpRequest trace;

        protected Message.Extract extract;

        public String getUri() {
            return uri;
        }

        @SchemaProperty(advanced = true, description = "Http request URI.")
        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getFork() {
            return fork;
        }

        @SchemaProperty(advanced = true, description = "When enabled the send operation does not block while waiting for the response.")
        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        public HttpRequest getGET() {
            return get;
        }

        @SchemaProperty(name = "GET", description = "Http GET request")
        public void setGET(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPOST() {
            return post;
        }

        @SchemaProperty(name = "POST", description = "Http POST request")
        public void setPOST(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPUT() {
            return put;
        }

        @SchemaProperty(name = "PUT", description = "Http PUT request")
        public void setPUT(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDELETE() {
            return delete;
        }

        @SchemaProperty(name = "DELETE", description = "Http DELETE request")
        public void setDELETE(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHEAD() {
            return head;
        }

        @SchemaProperty(name = "HEAD", description = "Http HEAD request")
        public void setHEAD(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOPTIONS() {
            return options;
        }

        @SchemaProperty(name = "OPTIONS", description = "Http OPTIONS request")
        public void setOPTIONS(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPATCH() {
            return patch;
        }

        @SchemaProperty(name = "PATCH", description = "Http PATCH request")
        public void setPATCH(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTRACE() {
            return trace;
        }

        @SchemaProperty(name = "TRACE", description = "Http TRACE request")
        public void setTRACE(HttpRequest trace) {
            this.trace = trace;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @SchemaType(oneOf = {
        "GET",
        "POST",
        "PUT",
        "PUT",
        "DELETE",
        "HEAD",
        "OPTIONS",
        "PATCH",
        "TRACE"
    })
    public static class ServerRequest {
        protected Integer timeout;
        protected String select;
        protected String validator;
        protected String validators;
        protected String headerValidator;
        protected String headerValidators;

        protected HttpRequest get;
        protected HttpRequest post;
        protected HttpRequest put;
        protected HttpRequest delete;
        protected HttpRequest head;
        protected HttpRequest options;
        protected HttpRequest patch;
        protected HttpRequest trace;

        protected Receive.Selector selector;

        protected List<Receive.Validate> validate;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        @SchemaProperty(description = "Http request timeout.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getSelect() {
            return select;
        }

        @SchemaProperty(advanced = true, description = "Message select expression to selectively consume messages.")
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

        public HttpRequest getGET() {
            return get;
        }

        @SchemaProperty(name = "GET", description = "Http GET request")
        public void setGET(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPOST() {
            return post;
        }

        @SchemaProperty(name = "POST", description = "Http POST request")
        public void setPOST(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPUT() {
            return put;
        }

        @SchemaProperty(name = "PUT", description = "Http PUT request")
        public void setPUT(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDELETE() {
            return delete;
        }

        @SchemaProperty(name = "DELETE", description = "Http DELETE request")
        public void setDELETE(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHEAD() {
            return head;
        }

        @SchemaProperty(name = "HEAD", description = "Http HEAD request")
        public void setHEAD(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOPTIONS() {
            return options;
        }

        @SchemaProperty(name = "OPTIONS", description = "Http OPTIONS request")
        public void setOPTIONS(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPATCH() {
            return patch;
        }

        @SchemaProperty(name = "PATCH", description = "Http PATCH request")
        public void setPATCH(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTRACE() {
            return trace;
        }

        @SchemaProperty(name = "TRACE", description = "Http TRACE request")
        public void setTRACE(HttpRequest trace) {
            this.trace = trace;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        @SchemaProperty(advanced = true, description = "Message selector expression to selectively consume messages.")
        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
        }

        public List<Receive.Validate> getValidate() {
            if (validate == null) {
                validate = new ArrayList<>();
            }

            return validate;
        }

        @SchemaProperty(description = "Validate expressions.")
        public void setValidate(List<Receive.Validate> validate) {
            this.validate = validate;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ServerResponse {
        protected HttpResponse response;
        protected Message.Extract extract;

        public HttpResponse getResponse() {
            return response;
        }

        @SchemaProperty(description = "Http response message.")
        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class ClientResponse {
        protected Integer timeout;
        protected String select;
        protected String validator;
        protected String validators;

        protected String headerValidator;
        protected String headerValidators;

        protected Receive.Selector selector;

        protected HttpResponse response;

        protected List<Receive.Validate> validate;

        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        @SchemaProperty(description = "Timeout while waiting for the Http response.")
        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
        }

        public String getSelect() {
            return select;
        }

        @SchemaProperty(advanced = true, description = "Message selector expression to selectively consume the Http response.")
        public void setSelect(String select) {
            this.select = select;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        @SchemaProperty(advanced = true, description = "Message selector to selectively consume the Http response.")
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

        public HttpResponse getResponse() {
            return response;
        }

        @SchemaProperty(description = "The expected Http response.")
        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public List<Receive.Validate> getValidate() {
            if (validate == null) {
                validate = new ArrayList<>();
            }

            return validate;
        }

        @SchemaProperty(description = "Validate expressions.")
        public void setValidate(List<Receive.Validate> validate) {
            this.validate = validate;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        @SchemaProperty(advanced = true, description = "Extract message content to test variables.")
        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    public static class HttpResponse extends Message {
        protected String status = "200";
        protected String reasonPhrase;
        protected String version;
        protected String contentType;

        public String getStatus() {
            return status;
        }

        @SchemaProperty(description = "The Http status code.")
        public void setStatus(String status) {
            this.status = status;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        @SchemaProperty(advanced = true, description = "Http reason phrase.")
        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public String getVersion() {
            return version;
        }

        @SchemaProperty(advanced = true, description = "Http version.")
        public void setVersion(String version) {
            this.version = version;
        }

        public String getContentType() {
            return contentType;
        }

        @SchemaProperty(description = "Http content type.")
        public void setContentType(String contentType) {
                this.contentType = contentType;
            }
    }

    public static class HttpRequest extends Message {
        protected String path;
        protected String contentType;
        protected String accept;
        protected String version;

        protected List<QueryParameter> parameters;

        public String getPath() {
            return path;
        }

        @SchemaProperty(description = "Http request path.")
        public void setPath(String path) {
            this.path = path;
        }

        public String getContentType() {
            return contentType;
        }

        @SchemaProperty(description = "Http content type.")
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getAccept() {
            return accept;
        }

        @SchemaProperty(advanced = true, description = "Http accept header.")
        public void setAccept(String accept) {
            this.accept = accept;
        }

        public String getVersion() {
            return version;
        }

        @SchemaProperty(advanced = true, description = "Http version.")
        public void setVersion(String version) {
            this.version = version;
        }

        public List<QueryParameter> getParameters() {
            if (parameters == null) {
                parameters = new ArrayList<>();
            }
            return this.parameters;
        }

        @SchemaProperty(advanced = true, description = "List of Http query parameters.")
        public void setParameters(List<QueryParameter> parameters) {
            this.parameters = parameters;
        }

        public static class QueryParameter {
            protected String name;
            protected String value;

            public String getName() {
                return name;
            }

            @SchemaProperty(required = true, description = "Http query parameter name.")
            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            @SchemaProperty(required = true, description = "Http query parameter value.")
            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
