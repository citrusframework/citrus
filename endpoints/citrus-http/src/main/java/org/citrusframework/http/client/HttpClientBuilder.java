/*
 * Copyright 2006-2016 the original author or authors.
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

import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.http.security.HttpAuthentication;
import org.citrusframework.http.security.HttpSecureConnection;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HttpClientBuilder extends AbstractEndpointBuilder<HttpClient> {

    /** Endpoint target */
    private final HttpClient endpoint = new HttpClient();

    @Override
    protected HttpClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the requestUrl property.
     * @param uri
     * @return
     */
    public HttpClientBuilder requestUrl(String uri) {
        endpoint.getEndpointConfiguration().setRequestUrl(uri);
        return this;
    }

    /**
     * Sets the rest template.
     * @param restTemplate
     * @return
     */
    public HttpClientBuilder restTemplate(RestTemplate restTemplate) {
        endpoint.getEndpointConfiguration().setRestTemplate(restTemplate);
        return this;
    }

    /**
     * Sets the request factory.
     * @param requestFactory
     * @return
     */
    public HttpClientBuilder requestFactory(ClientHttpRequestFactory requestFactory) {
        endpoint.getEndpointConfiguration().setRequestFactory(requestFactory);
        return this;
    }

    /**
     * Sets the request method.
     * @param requestMethod
     * @return
     */
    public HttpClientBuilder requestMethod(RequestMethod requestMethod) {
        endpoint.getEndpointConfiguration().setRequestMethod(requestMethod);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public HttpClientBuilder messageConverter(HttpMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public HttpClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the endpoint uri resolver.
     * @param resolver
     * @return
     */
    public HttpClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointUriResolver(resolver);
        return this;
    }

    /**
     * Sets the default charset.
     * @param charset
     * @return
     */
    public HttpClientBuilder charset(String charset) {
        endpoint.getEndpointConfiguration().setCharset(charset);
        return this;
    }

    /**
     * Sets the default accept header.
     * @param flag
     * @return
     */
    public HttpClientBuilder defaultAcceptHeader(boolean flag) {
        endpoint.getEndpointConfiguration().setDefaultAcceptHeader(flag);
        return this;
    }

    /**
     * Sets the handleCookies property.
     * @param flag
     * @return
     */
    public HttpClientBuilder handleCookies(boolean flag) {
        endpoint.getEndpointConfiguration().setHandleCookies(flag);
        return this;
    }

    /**
     * Sets the content type.
     * @param contentType
     * @return
     */
    public HttpClientBuilder contentType(String contentType) {
        endpoint.getEndpointConfiguration().setContentType(contentType);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public HttpClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the error handling strategy.
     * @param errorStrategy
     * @return
     */
    public HttpClientBuilder errorHandlingStrategy(ErrorHandlingStrategy errorStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(errorStrategy);
        return this;
    }

    /**
     * Sets the error handler.
     * @param errorHandler
     * @return
     */
    public HttpClientBuilder errorHandler(ResponseErrorHandler errorHandler) {
        endpoint.getEndpointConfiguration().setErrorHandler(errorHandler);
        return this;
    }

    /**
     * Sets the client interceptors.
     * @param interceptors
     * @return
     */
    public HttpClientBuilder interceptors(List<ClientHttpRequestInterceptor> interceptors) {
        endpoint.getEndpointConfiguration().setClientInterceptors(interceptors);
        return this;
    }

    /**
     * Sets the binaryMediaTypes.
     * @param binaryMediaTypes
     * @return
     */
    public HttpClientBuilder binaryMediaTypes(List<MediaType> binaryMediaTypes) {
        endpoint.getEndpointConfiguration().setBinaryMediaTypes(binaryMediaTypes);
        return this;
    }

    /**
     * Sets a client single interceptor.
     * @param interceptor
     * @return
     */
    public HttpClientBuilder interceptor(ClientHttpRequestInterceptor interceptor) {
        endpoint.getEndpointConfiguration().getClientInterceptors().add(interceptor);
        return this;
    }

    /**
     * Sets the header mapper.
     * @param headerMapper
     * @return
     */
    public HttpClientBuilder headerMapper(DefaultHttpHeaderMapper headerMapper) {
        endpoint.getEndpointConfiguration().setHeaderMapper(headerMapper);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public HttpClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    /**
     * Sets the user authentication.
     * @param auth
     * @return
     */
    public HttpClientBuilder authentication(HttpAuthentication auth) {
        endpoint.getEndpointConfiguration().setRequestFactory(auth.getRequestFactory(endpoint.getEndpointConfiguration().getRequestUrl(), endpoint));
        return this;
    }

    /**
     * Enable secured connection on the client using provided SSL connection.
     * @return
     */
    public HttpClientBuilder secured(HttpSecureConnection conn) {
        endpoint.getEndpointConfiguration()
                .getHttpClient()
                .setConnectionManager(conn.getClientConnectionManager());
        return this;
    }
}
