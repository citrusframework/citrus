/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.http.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
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
import org.citrusframework.xml.actions.Message;
import org.citrusframework.xml.actions.Receive;
import org.citrusframework.xml.actions.Send;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "http")
public class Http implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> builder;

    private Receive receive;
    private Send send;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    @XmlElement
    public Http setDescription(String value) {
        this.description = value;
        return this;
    }

    @XmlAttribute(name = "actor")
    public Http setActor(String actor) {
        this.actor = actor;
        return this;
    }

    @XmlAttribute(name = "client")
    public Http setHttpClient(String httpClient) {
        builder = new HttpActionBuilder().client(httpClient);
        return this;
    }

    @XmlAttribute(name = "server")
    public Http setHttpServer(String httpServer) {
        builder = new HttpActionBuilder().server(httpServer);
        return this;
    }

    @XmlElement(name = "send-request")
    public Http setSendRequest(ClientRequest request) {
        HttpClientRequestActionBuilder requestBuilder;
        HttpRequest requestMessage;
        if (request.getGet() != null) {
            requestMessage = request.getGet();
            requestBuilder = asClientBuilder().send().get(requestMessage.path);
        } else if (request.getPost() != null) {
            requestMessage = request.getPost();
            requestBuilder = asClientBuilder().send().post(requestMessage.path);
        } else if (request.getPut() != null) {
            requestMessage = request.getPut();
            requestBuilder = asClientBuilder().send().put(requestMessage.path);
        } else if (request.getDelete() != null) {
            requestMessage = request.getDelete();
            requestBuilder = asClientBuilder().send().delete(requestMessage.path);
        } else if (request.getHead() != null) {
            requestMessage = request.getHead();
            requestBuilder = asClientBuilder().send().head(requestMessage.path);
        } else if (request.getOptions() != null) {
            requestMessage = request.getOptions();
            requestBuilder = asClientBuilder().send().options(requestMessage.path);
        } else if (request.getPatch() != null) {
            requestMessage = request.getPatch();
            requestBuilder = asClientBuilder().send().patch(requestMessage.path);
        } else if (request.getTrace() != null) {
            requestMessage = request.getTrace();
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
        return this;
    }

    @XmlElement(name = "receive-response")
    public Http setReceiveResponse(ClientResponse response) {
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

            response.getValidates().forEach(receive.getValidates()::add);

            if (response.extract != null) {
                receive.setExtract(response.extract);
            }
        }

        builder = responseBuilder;
        return this;
    }

    @XmlElement(name = "receive-request")
    public Http setReceiveRequest(ServerRequest request) {
        HttpServerRequestActionBuilder requestBuilder;
        HttpRequest requestMessage;
        if (request.getGet() != null) {
            requestMessage = request.getGet();
            requestBuilder = asServerBuilder().receive().get(requestMessage.path);
        } else if (request.getPost() != null) {
            requestMessage = request.getPost();
            requestBuilder = asServerBuilder().receive().post(requestMessage.path);
        } else if (request.getPut() != null) {
            requestMessage = request.getPut();
            requestBuilder = asServerBuilder().receive().put(requestMessage.path);
        } else if (request.getDelete() != null) {
            requestMessage = request.getDelete();
            requestBuilder = asServerBuilder().receive().delete(requestMessage.path);
        } else if (request.getHead() != null) {
            requestMessage = request.getHead();
            requestBuilder = asServerBuilder().receive().head(requestMessage.path);
        } else if (request.getOptions() != null) {
            requestMessage = request.getOptions();
            requestBuilder = asServerBuilder().receive().options(requestMessage.path);
        } else if (request.getPatch() != null) {
            requestMessage = request.getPatch();
            requestBuilder = asServerBuilder().receive().patch(requestMessage.path);
        } else if (request.getTrace() != null) {
            requestMessage = request.getTrace();
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

        request.getValidates().forEach(receive.getValidates()::add);

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
        return this;
    }

    @XmlElement(name = "send-response")
    public Http setSendResponse(ServerResponse response) {
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
        return this;
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
    private HttpClientActionBuilder asClientBuilder() {
        if (builder instanceof HttpClientActionBuilder) {
            return (HttpClientActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to http client action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    /**
     * Converts current builder to server builder.
     * @return
     */
    private HttpServerActionBuilder asServerBuilder() {
        if (builder instanceof HttpServerActionBuilder) {
            return (HttpServerActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to http server action builder",
                Optional.ofNullable(builder).map(Object::getClass).map(Class::getName).orElse("null")));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "get",
            "post",
            "put",
            "delete",
            "head",
            "options",
            "patch",
            "trace",
            "extract"
    })
    public static class ClientRequest {
        @XmlAttribute(name = "uri")
        protected String uri;
        @XmlAttribute(name = "fork")
        protected Boolean fork;

        @XmlElement(name = "GET")
        protected HttpRequest get;
        @XmlElement(name = "POST")
        protected HttpRequest post;
        @XmlElement(name = "PUT")
        protected HttpRequest put;
        @XmlElement(name = "DELETE")
        protected HttpRequest delete;
        @XmlElement(name = "HEAD")
        protected HttpRequest head;
        @XmlElement(name = "OPTIONS")
        protected HttpRequest options;
        @XmlElement(name = "PATCH")
        protected HttpRequest patch;
        @XmlElement(name = "TRACE")
        protected HttpRequest trace;

        @XmlElement
        protected Message.Extract extract;

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

        public HttpRequest getGet() {
            return get;
        }

        public void setGet(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPost() {
            return post;
        }

        public void setPost(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPut() {
            return put;
        }

        public void setPut(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDelete() {
            return delete;
        }

        public void setDelete(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHead() {
            return head;
        }

        public void setHead(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOptions() {
            return options;
        }

        public void setOptions(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPatch() {
            return patch;
        }

        public void setPatch(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTrace() {
            return trace;
        }

        public void setTrace(HttpRequest trace) {
            this.trace = trace;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "selector",
            "get",
            "post",
            "put",
            "delete",
            "head",
            "options",
            "patch",
            "trace",
            "validates",
            "extract"
    })
    public static class ServerRequest {
        @XmlAttribute
        protected Integer timeout;

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

        @XmlElement(name = "GET")
        protected HttpRequest get;
        @XmlElement(name = "POST")
        protected HttpRequest post;
        @XmlElement(name = "PUT")
        protected HttpRequest put;
        @XmlElement(name = "DELETE")
        protected HttpRequest delete;
        @XmlElement(name = "HEAD")
        protected HttpRequest head;
        @XmlElement(name = "OPTIONS")
        protected HttpRequest options;
        @XmlElement(name = "PATCH")
        protected HttpRequest patch;
        @XmlElement(name = "TRACE")
        protected HttpRequest trace;

        @XmlElement
        protected Receive.Selector selector;

        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates;

        @XmlElement
        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
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

        public HttpRequest getGet() {
            return get;
        }

        public void setGet(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPost() {
            return post;
        }

        public void setPost(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPut() {
            return put;
        }

        public void setPut(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDelete() {
            return delete;
        }

        public void setDelete(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHead() {
            return head;
        }

        public void setHead(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOptions() {
            return options;
        }

        public void setOptions(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPatch() {
            return patch;
        }

        public void setPatch(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTrace() {
            return trace;
        }

        public void setTrace(HttpRequest trace) {
            this.trace = trace;
        }

        public Receive.Selector getSelector() {
            return selector;
        }

        public void setSelector(Receive.Selector selector) {
            this.selector = selector;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "response",
            "extract"
    })
    public static class ServerResponse {
        @XmlElement
        protected HttpResponse response;

        @XmlElement
        protected Message.Extract extract;

        public HttpResponse getResponse() {
            return response;
        }

        public void setResponse(HttpResponse response) {
            this.response = response;
        }

        public Message.Extract getExtract() {
            return extract;
        }

        public void setExtract(Message.Extract extract) {
            this.extract = extract;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "selector",
            "response",
            "validates",
            "extract"
    })
    public static class ClientResponse {
        @XmlAttribute
        protected Integer timeout;

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
        protected HttpResponse response;

        @XmlElement(name = "validate")
        protected List<Receive.Validate> validates;

        @XmlElement
        protected Message.Extract extract;

        public Integer getTimeout() {
            return timeout;
        }

        public void setTimeout(Integer timeout) {
            this.timeout = timeout;
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

        public HttpResponse getResponse() {
            return response;
        }

        public void setResponse(HttpResponse response) {
            this.response = response;
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class HttpResponse extends Message {
        @XmlAttribute(name = "status")
        protected String status = "200";
        @XmlAttribute(name = "reason-phrase")
        protected String reasonPhrase;
        @XmlAttribute(name = "version")
        protected String version;
        @XmlAttribute(name = "content-type")
        protected String contentType;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public void setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
                this.contentType = contentType;
            }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "parameters"
    })
    public static class HttpRequest extends Message {
        @XmlAttribute
        protected String path;
        @XmlAttribute(name = "content-type")
        protected String contentType;
        @XmlAttribute(name = "accept")
        protected String accept;
        @XmlAttribute(name = "version")
        protected String version;

        @XmlElement(name = "param")
        protected List<QueryParameter> parameters;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getAccept() {
            return accept;
        }

        public void setAccept(String accept) {
            this.accept = accept;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public List<QueryParameter> getParameters() {
            if (parameters == null) {
                parameters = new ArrayList<>();
            }
            return this.parameters;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class QueryParameter {
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value")
            protected String value;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

}
