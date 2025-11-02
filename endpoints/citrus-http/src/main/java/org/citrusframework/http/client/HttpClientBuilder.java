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

package org.citrusframework.http.client;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.yaml.SchemaProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @since 2.5
 */
public class HttpClientBuilder extends AbstractEndpointBuilder<HttpClient> {

    /** Endpoint target */
    private final HttpClient endpoint = new HttpClient();

    private String restTemplate;
    private String requestFactory;
    private String messageConverter;
    private String correlator;
    private String endpointResolver;
    private String errorHandler;
    private String headerMapper;
    private String authentication;
    private String securedConnection;
    private final List<String> interceptors = new ArrayList<>();

    @Override
    public HttpClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(restTemplate)) {
                restTemplate(referenceResolver.resolve(restTemplate, RestTemplate.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, HttpMessageConverter.class));
            }

            if (StringUtils.hasText(requestFactory)) {
                requestFactory(referenceResolver.resolve(requestFactory, ClientHttpRequestFactory.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(endpointResolver)) {
                endpointResolver(referenceResolver.resolve(endpointResolver, EndpointUriResolver.class));
            }

            if (StringUtils.hasText(errorHandler)) {
                errorHandler(referenceResolver.resolve(errorHandler, ResponseErrorHandler.class));
            }

            if (StringUtils.hasText(headerMapper)) {
                headerMapper(referenceResolver.resolve(headerMapper, HeaderMapper.class));
            }

            if (StringUtils.hasText(authentication)) {
                authentication(referenceResolver.resolve(authentication, HttpAuthentication.class));
            }

            if (StringUtils.hasText(securedConnection)) {
                secured(referenceResolver.resolve(securedConnection, HttpSecureConnection.class));
            }

            for (String interceptor : interceptors) {
                interceptor(referenceResolver.resolve(interceptor, ClientHttpRequestInterceptor.class));
            }
        }

        return super.build();
    }

    @Override
    protected HttpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the requestUrl property.
     */
    public HttpClientBuilder requestUrl(String uri) {
        endpoint.getEndpointConfiguration().setRequestUrl(uri);
        return this;
    }

    @SchemaProperty(description = "Sets the Http client request URL.")
    public void setRequestUrl(String uri) {
        requestUrl(uri);
    }

    /**
     * Sets the rest template.
     */
    public HttpClientBuilder restTemplate(RestTemplate restTemplate) {
        endpoint.getEndpointConfiguration().setRestTemplate(restTemplate);
        return this;
    }

    @SchemaProperty(advanced = true, description = "The reference to a REST template bean.")
    public void setRestTemplate(String restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the request factory.
     */
    public HttpClientBuilder requestFactory(ClientHttpRequestFactory requestFactory) {
        endpoint.getEndpointConfiguration().setRequestFactory(requestFactory);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Reference to a request factory.")
    public void setRequestFactory(String requestFactory) {
        this.requestFactory = requestFactory;
    }

    /**
     * Sets the request method.
     */
    public HttpClientBuilder requestMethod(RequestMethod requestMethod) {
        endpoint.getEndpointConfiguration().setRequestMethod(requestMethod);
        return this;
    }

    @SchemaProperty(description = "The default request method to use")
    public void setRequestMethod(RequestMethod requestMethod) {
        requestMethod(requestMethod);
    }

    /**
     * Sets the message converter.
     */
    public HttpClientBuilder messageConverter(HttpMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Bean reference to a message converter.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the message correlator.
     */
    public HttpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the endpoint uri resolver.
     */
    public HttpClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointUriResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the endpoint URI resolver.")
    public void setEndpointResolver(String resolver) {
        this.endpointResolver = resolver;
    }

    /**
     * Sets the default charset.
     */
    public HttpClientBuilder charset(String charset) {
        endpoint.getEndpointConfiguration().setCharset(charset);
        return this;
    }

    @SchemaProperty(advanced = true, description = "The default charset.")
    public void setCharset(String charset) {
        charset(charset);
    }

    /**
     * Sets the default accept header.
     */
    public HttpClientBuilder defaultAcceptHeader(boolean flag) {
        endpoint.getEndpointConfiguration().setDefaultAcceptHeader(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the client adds the default accept header to requests.")
    public void setDefaultAcceptHeader(boolean flag) {
        defaultAcceptHeader(flag);
    }

    /**
     * Sets the handleCookies property.
     */
    public HttpClientBuilder handleCookies(boolean flag) {
        endpoint.getEndpointConfiguration().setHandleCookies(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the client handles cookies.")
    public void setHandleCookies(boolean flag) {
        handleCookies(flag);
    }

    /**
     * Sets the disabled redirect handling property.
     */
    public HttpClientBuilder disableRedirectHandling(boolean flag) {
        endpoint.getEndpointConfiguration().setDisableRedirectHandling(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Disables the redirect handling for this client.")
    public void setDisableRedirectHandling(boolean flag) {
        disableRedirectHandling(flag);
    }

    /**
     * Sets the content type.
     */
    public HttpClientBuilder contentType(String contentType) {
        endpoint.getEndpointConfiguration().setContentType(contentType);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the default content type header set for each request.")
    public void setContentType(String contentType) {
        contentType(contentType);
    }

    /**
     * Sets the polling interval.
     */
    public HttpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the error handling strategy.
     */
    public HttpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandling") },
            description = "Sets the error handling strategy.")
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        errorHandlingStrategy(errorStrategy);
    }

    /**
     * Sets the error handler.
     */
    public HttpClientBuilder errorHandler(ResponseErrorHandler errorHandler) {
        endpoint.getEndpointConfiguration().setErrorHandler(errorHandler);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandling") },
            description = "Sets a custom error handler."
    )
    public void setErrorHandler(String errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sets the client interceptors.
     */
    public HttpClientBuilder interceptors(List<ClientHttpRequestInterceptor> interceptors) {
        endpoint.getEndpointConfiguration().setClientInterceptors(interceptors);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets the list of client interceptor bean references.")
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    /**
     * Sets the binaryMediaTypes.
     */
    public HttpClientBuilder binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.getEndpointConfiguration().setBinaryMediaTypes(binaryMediaTypes);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the list of binary media types.")
    public void setBinaryMediaTypes(List<String> binaryMediaTypes) {
        binaryMediaTypes(binaryMediaTypes.stream().map(MediaType::valueOf).toList());
    }

    /**
     * Sets a client single interceptor.
     */
    public HttpClientBuilder interceptor(ClientHttpRequestInterceptor interceptor) {
        endpoint.getEndpointConfiguration().getClientInterceptors().add(interceptor);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets a client interceptor.")
    public void setInterceptor(String interceptor) {
        this.interceptors.add(interceptor);
    }

    /**
     * Sets the header mapper.
     */
    public HttpClientBuilder headerMapper(HeaderMapper<HttpHeaders> headerMapper) {
        endpoint.getEndpointConfiguration().setHeaderMapper(headerMapper);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom header mapper bean reference.")
    public void setHeaderMapper(String headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the default timeout.
     */
    public HttpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "The Http request timeout while waiting for a response", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }

    /**
     * Sets the user authentication.
     */
    public HttpClientBuilder authentication(HttpAuthentication auth) {
        endpoint.getEndpointConfiguration().setRequestFactory(auth.getRequestFactory(endpoint.getEndpointConfiguration().getRequestUrl(), endpoint));
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Use given authentication mechanism.")
    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }

    /**
     * Enable secured connection on the client using provided SSL connection.
     */
    public HttpClientBuilder secured(HttpSecureConnection conn) {
        endpoint.getEndpointConfiguration()
                .getHttpClient()
                .setConnectionManager(conn.getClientConnectionManager());
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:security") },
            description = "Secure the connection with given security mechanism.")
    public void setSecuredConnection(String connection) {
        this.securedConnection = connection;
    }
}
