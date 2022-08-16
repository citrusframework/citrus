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

package com.consol.citrus.http.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.consol.citrus.AbstractTestActionBuilder;
import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.TestActor;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.actions.HttpActionBuilder;
import com.consol.citrus.http.actions.HttpClientActionBuilder;
import com.consol.citrus.http.actions.HttpClientRequestActionBuilder;
import com.consol.citrus.http.actions.HttpClientResponseActionBuilder;
import com.consol.citrus.http.actions.HttpServerActionBuilder;
import com.consol.citrus.http.actions.HttpServerRequestActionBuilder;
import com.consol.citrus.http.actions.HttpServerResponseActionBuilder;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.xml.actions.Message;
import com.consol.citrus.xml.actions.MessageSupport;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "http")
public class Http implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private TestActionBuilder<?> builder;

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
    public Http setSendRequest(Request request) {
        HttpClientRequestActionBuilder requestBuilder;
        RequestMessage requestMessage;
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

        if (request.fork != null) {
            requestBuilder.fork(request.fork);
        }

        requestBuilder.name("http:send-request");
        requestBuilder.description(description);
        MessageSupport.configureMessage(requestBuilder, requestMessage);

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

        for (RequestMessage.QueryParameter param : requestMessage.getParameters()) {
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
    public Http setReceiveResponse(ResponseMessage response) {
        HttpClientResponseActionBuilder responseBuilder = asClientBuilder().receive().response().name("http:receive-response");

        responseBuilder.description(description);
        MessageSupport.configureMessage(responseBuilder, response);

        responseBuilder.message().header(HttpMessageHeaders.HTTP_STATUS_CODE, response.status);

        if (response.reasonPhrase != null) {
            responseBuilder.message().reasonPhrase(response.reasonPhrase);
        }

        if (response.contentType != null) {
            responseBuilder.message().contentType(response.contentType);
        }

        if (response.version != null) {
            responseBuilder.message().version(response.version);
        }

        builder = responseBuilder;
        return this;
    }

    @XmlElement(name = "receive-request")
    public Http setReceiveRequest(Request request) {
        HttpServerRequestActionBuilder requestBuilder;
        RequestMessage requestMessage;
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
        MessageSupport.configureMessage(requestBuilder, requestMessage);

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

        for (RequestMessage.QueryParameter param : requestMessage.getParameters()) {
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
    public Http setSendResponse(ResponseMessage response) {
        HttpServerResponseActionBuilder responseBuilder = asServerBuilder().send().response().name("http:send-response");

        responseBuilder.description(description);
        MessageSupport.configureMessage(responseBuilder, response);

        responseBuilder.message().header(HttpMessageHeaders.HTTP_STATUS_CODE, response.status);

        if (response.reasonPhrase != null) {
            responseBuilder.message().reasonPhrase(response.reasonPhrase);
        }

        if (response.contentType != null) {
            responseBuilder.message().contentType(response.contentType);
        }

        if (response.version != null) {
            responseBuilder.message().version(response.version);
        }

        builder = responseBuilder;
        return this;
    }

    @Override
    public TestAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing client or server Http action - please provide proper action details");
        }

        if (referenceResolver != null) {
            if (builder instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
            }

            if (actor != null && builder instanceof AbstractTestActionBuilder) {
                ((AbstractTestActionBuilder<?, ?>) builder).actor(referenceResolver.resolve(actor, TestActor.class));
            }
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
     * Converts current builder to client builder.
     * @return
     */
    private HttpServerActionBuilder asServerBuilder() {
        if (builder instanceof HttpServerActionBuilder) {
            return (HttpServerActionBuilder) builder;
        }

        throw new CitrusRuntimeException(String.format("Failed to convert '%s' to http client action builder",
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
    })
    public static class Request {
        @XmlAttribute(name = "uri")
        protected String uri;
        @XmlAttribute(name = "fork")
        protected Boolean fork;

        @XmlElement(name = "GET")
        protected RequestMessage get;
        @XmlElement(name = "POST")
        protected RequestMessage post;
        @XmlElement(name = "PUT")
        protected RequestMessage put;
        @XmlElement(name = "DELETE")
        protected RequestMessage delete;
        @XmlElement(name = "HEAD")
        protected RequestMessage head;
        @XmlElement(name = "OPTIONS")
        protected RequestMessage options;
        @XmlElement(name = "PATCH")
        protected RequestMessage patch;
        @XmlElement(name = "TRACE")
        protected RequestMessage trace;

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

        public RequestMessage getGet() {
            return get;
        }

        public void setGet(RequestMessage get) {
            this.get = get;
        }

        public RequestMessage getPost() {
            return post;
        }

        public void setPost(RequestMessage post) {
            this.post = post;
        }

        public RequestMessage getPut() {
            return put;
        }

        public void setPut(RequestMessage put) {
            this.put = put;
        }

        public RequestMessage getDelete() {
            return delete;
        }

        public void setDelete(RequestMessage delete) {
            this.delete = delete;
        }

        public RequestMessage getHead() {
            return head;
        }

        public void setHead(RequestMessage head) {
            this.head = head;
        }

        public RequestMessage getOptions() {
            return options;
        }

        public void setOptions(RequestMessage options) {
            this.options = options;
        }

        public RequestMessage getPatch() {
            return patch;
        }

        public void setPatch(RequestMessage patch) {
            this.patch = patch;
        }

        public RequestMessage getTrace() {
            return trace;
        }

        public void setTrace(RequestMessage trace) {
            this.trace = trace;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class ResponseMessage extends Message {
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
    public static class RequestMessage extends Message {
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
