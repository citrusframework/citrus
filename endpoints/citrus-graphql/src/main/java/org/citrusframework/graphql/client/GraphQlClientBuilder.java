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
package org.citrusframework.graphql.client;

import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.api.yaml.SchemaProperty;
import org.citrusframework.api.yaml.SchemaType;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * Builds a {@link GraphQlClient}. Every HTTP client setting of {@link HttpClientBuilder} applies; the
 * GraphQL settings are the path, the schema resources and strict document checks.
 */
@SchemaType(module = "citrus-graphql")
@XmlType(name = "", propOrder = {})
public class GraphQlClientBuilder extends HttpClientBuilder {

    public GraphQlClientBuilder() {
        super(new GraphQlClient());
    }

    @Override
    public GraphQlClient build() {
        return (GraphQlClient) super.build();
    }

    @Override
    protected GraphQlClient getEndpoint() {
        return (GraphQlClient) super.getEndpoint();
    }

    /**
     * Sets the GraphQL path appended to the request URL.
     */
    public GraphQlClientBuilder path(String path) {
        getEndpoint().getEndpointConfiguration().setPath(path);
        return this;
    }

    @SchemaProperty(description = "The GraphQL path appended to the request URL. Defaults to /graphql unless the request URL has a path.")
    @XmlAttribute
    public void setPath(String path) {
        path(path);
    }

    /**
     * Sets the SDL schema resources used to validate GraphQL documents.
     */
    public GraphQlClientBuilder schema(String... resources) {
        getEndpoint().getEndpointConfiguration().setSchemaResources(Arrays.asList(resources));
        return this;
    }

    @SchemaProperty(description = "The SDL schema resources used to validate GraphQL documents.")
    @XmlAttribute
    public void setSchema(List<String> resources) {
        getEndpoint().getEndpointConfiguration().setSchemaResources(resources);
    }

    /**
     * Enables or disables GraphQL document and response checks (default enabled).
     */
    public GraphQlClientBuilder strict(boolean strict) {
        getEndpoint().getEndpointConfiguration().setStrict(strict);
        return this;
    }

    @SchemaProperty(description = "Enables or disables GraphQL document and response checks.", defaultValue = "true")
    @XmlAttribute
    public void setStrict(boolean strict) {
        strict(strict);
    }

    @Override
    public GraphQlClientBuilder requestUrl(String uri) {
        super.requestUrl(uri);
        return this;
    }

    @Override
    public GraphQlClientBuilder restTemplate(RestTemplate restTemplate) {
        super.restTemplate(restTemplate);
        return this;
    }

    @Override
    public GraphQlClientBuilder requestFactory(ClientHttpRequestFactory requestFactory) {
        super.requestFactory(requestFactory);
        return this;
    }

    @Override
    public GraphQlClientBuilder requestMethod(RequestMethod requestMethod) {
        super.requestMethod(requestMethod);
        return this;
    }

    @Override
    public GraphQlClientBuilder messageConverter(HttpMessageConverter messageConverter) {
        super.messageConverter(messageConverter);
        return this;
    }

    @Override
    public GraphQlClientBuilder correlator(MessageCorrelator correlator) {
        super.correlator(correlator);
        return this;
    }

    @Override
    public GraphQlClientBuilder endpointResolver(EndpointUriResolver resolver) {
        super.endpointResolver(resolver);
        return this;
    }

    @Override
    public GraphQlClientBuilder charset(String charset) {
        super.charset(charset);
        return this;
    }

    @Override
    public GraphQlClientBuilder defaultAcceptHeader(boolean flag) {
        super.defaultAcceptHeader(flag);
        return this;
    }

    @Override
    public GraphQlClientBuilder handleCookies(boolean flag) {
        super.handleCookies(flag);
        return this;
    }

    @Override
    public GraphQlClientBuilder disableRedirectHandling(boolean flag) {
        super.disableRedirectHandling(flag);
        return this;
    }

    @Override
    public GraphQlClientBuilder contentType(String contentType) {
        super.contentType(contentType);
        return this;
    }

    @Override
    public GraphQlClientBuilder pollingInterval(int pollingInterval) {
        super.pollingInterval(pollingInterval);
        return this;
    }

    @Override
    public GraphQlClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        super.errorHandlingStrategy(errorStrategy);
        return this;
    }

    @Override
    public GraphQlClientBuilder errorHandler(ResponseErrorHandler errorHandler) {
        super.errorHandler(errorHandler);
        return this;
    }

    @Override
    public GraphQlClientBuilder interceptors(List<ClientHttpRequestInterceptor> interceptors) {
        super.interceptors(interceptors);
        return this;
    }

    @Override
    public GraphQlClientBuilder binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        super.binaryMediaTypes(binaryMediaTypes);
        return this;
    }

    @Override
    public GraphQlClientBuilder interceptor(ClientHttpRequestInterceptor interceptor) {
        super.interceptor(interceptor);
        return this;
    }

    @Override
    public GraphQlClientBuilder headerMapper(HeaderMapper<HttpHeaders> headerMapper) {
        super.headerMapper(headerMapper);
        return this;
    }

    @Override
    public GraphQlClientBuilder timeout(long timeout) {
        super.timeout(timeout);
        return this;
    }

    @Override
    public GraphQlClientBuilder authentication(HttpAuthentication auth) {
        super.authentication(auth);
        return this;
    }

    @Override
    public GraphQlClientBuilder secured(HttpSecureConnection conn) {
        super.secured(conn);
        return this;
    }}
