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

package org.citrusframework.http.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import org.citrusframework.yaml.actions.Message;
import org.citrusframework.yaml.actions.Receive;
import org.citrusframework.yaml.actions.Send;

/**
 * @author Christoph Deppisch
 */
public class Http implements TestActionBuilder<TestAction>, ReferenceResolverAware {

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

    public void setClient(String httpClient) {
        builder = new HttpActionBuilder().client(httpClient);
    }

    public void setServer(String httpServer) {
        builder = new HttpActionBuilder().server(httpServer);
    }

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

        request.getValidates().forEach(receive.getValidate()::add);

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

        public void setUri(String uri) {
            this.uri = uri;
        }

        public Boolean getFork() {
            return fork;
        }

        public void setFork(Boolean fork) {
            this.fork = fork;
        }

        public HttpRequest getGET() {
            return get;
        }

        public void setGET(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPOST() {
            return post;
        }

        public void setPOST(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPUT() {
            return put;
        }

        public void setPUT(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDELETE() {
            return delete;
        }

        public void setDELETE(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHEAD() {
            return head;
        }

        public void setHEAD(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOPTIONS() {
            return options;
        }

        public void setOPTIONS(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPATCH() {
            return patch;
        }

        public void setPATCH(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTRACE() {
            return trace;
        }

        public void setTRACE(HttpRequest trace) {
            this.trace = trace;
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

        protected List<Receive.Validate> validates;

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

        public HttpRequest getGET() {
            return get;
        }

        public void setGET(HttpRequest get) {
            this.get = get;
        }

        public HttpRequest getPOST() {
            return post;
        }

        public void setPOST(HttpRequest post) {
            this.post = post;
        }

        public HttpRequest getPUT() {
            return put;
        }

        public void setPUT(HttpRequest put) {
            this.put = put;
        }

        public HttpRequest getDELETE() {
            return delete;
        }

        public void setDELETE(HttpRequest delete) {
            this.delete = delete;
        }

        public HttpRequest getHEAD() {
            return head;
        }

        public void setHEAD(HttpRequest head) {
            this.head = head;
        }

        public HttpRequest getOPTIONS() {
            return options;
        }

        public void setOPTIONS(HttpRequest options) {
            this.options = options;
        }

        public HttpRequest getPATCH() {
            return patch;
        }

        public void setPATCH(HttpRequest patch) {
            this.patch = patch;
        }

        public HttpRequest getTRACE() {
            return trace;
        }

        public void setTRACE(HttpRequest trace) {
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

        public Message.Extract getExtract() {
            return extract;
        }

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

        public List<Receive.Validate> getValidate() {
            if (validate == null) {
                validate = new ArrayList<>();
            }

            return validate;
        }

        public Message.Extract getExtract() {
            return extract;
        }

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

    public static class HttpRequest extends Message {
        protected String path;
        protected String contentType;
        protected String accept;
        protected String version;

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

        public void setParameters(List<QueryParameter> parameters) {
            this.parameters = parameters;
        }

        public static class QueryParameter {
            protected String name;
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
