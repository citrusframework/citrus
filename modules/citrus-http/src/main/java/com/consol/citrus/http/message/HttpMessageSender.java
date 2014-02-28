/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.http.message;

import com.consol.citrus.adapter.common.endpoint.EndpointUriResolver;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.integration.Message;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Message sender implementation sending messages over Http.
 * 
 * Note: Message sender is only using POST request method to publish
 * messages to the service endpoint.
 * 
 * @author Christoph Deppisch
 * @deprecated
 */
public class HttpMessageSender extends AbstractSyncMessageSender {

    /** New Http client */
    private final HttpClient httpClient;

    public HttpMessageSender() {
        this(new HttpClient());
    }

    public HttpMessageSender(HttpClient httpClient) {
        super(httpClient);
        this.httpClient = httpClient;
    }

    @Override
    public void send(Message<?> message) {
        httpClient.createProducer().send(message);
    }

    @Override
    public Consumer createConsumer() {
        return httpClient.createConsumer();
    }

    @Override
    public Producer createProducer() {
        return httpClient.createProducer();
    }

    @Override
    public EndpointConfiguration getEndpointConfiguration() {
        return httpClient.getEndpointConfiguration();
    }

    @Override
    public void setReplyMessageHandler(ReplyMessageHandler replyMessageHandler) {
        super.setReplyMessageHandler(replyMessageHandler);

        if (replyMessageHandler instanceof HttpReplyMessageReceiver) {
            ((HttpReplyMessageReceiver) replyMessageHandler).setEndpoint(httpClient);
        }
    }

    /**
     * Get the complete request URL.
     * @return the urlPath
     */
    public String getRequestUrl() {
        return httpClient.getEndpointConfiguration().getRequestUrl();
    }

    /**
     * Set the complete request URL.
     * @param url the url to set
     */
    public void setRequestUrl(String url) {
        httpClient.getEndpointConfiguration().setRequestUrl(url);
    }

    /**
     * Sets the request factory.
     * @param requestFactory
     */
    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        httpClient.getEndpointConfiguration().setRequestFactory(requestFactory);
    }

    /**
     * Gets the request factory.
     * @return
     */
    public ClientHttpRequestFactory getRequestFactory() {
        return httpClient.getEndpointConfiguration().getRequestFactory();
    }

    /**
     * Sets the endpoint uri resolver.
     * @param endpointUriResolver the endpointUriResolver to set
     */
    public void setEndpointUriResolver(EndpointUriResolver endpointUriResolver) {
        httpClient.getEndpointConfiguration().setEndpointUriResolver(endpointUriResolver);
    }

    /**
     * Sets the restTemplate.
     * @param restTemplate the restTemplate to set
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        httpClient.getEndpointConfiguration().setRestTemplate(restTemplate);
    }

    /**
     * Sets the requestMethod.
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(HttpMethod requestMethod) {
        httpClient.getEndpointConfiguration().setRequestMethod(requestMethod);
    }

    /**
     * Sets the charset.
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        httpClient.getEndpointConfiguration().setCharset(charset);
    }

    /**
     * Sets the headerMapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(HeaderMapper<HttpHeaders> headerMapper) {
        httpClient.getEndpointConfiguration().setHeaderMapper(headerMapper);
    }

    /**
     * Sets the contentType.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        httpClient.getEndpointConfiguration().setContentType(contentType);
    }

    /**
     * Gets the errorHandlingStrategy.
     * @return the errorHandlingStrategy
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return httpClient.getEndpointConfiguration().getErrorHandlingStrategy();
    }

    /**
     * Sets the errorHandlingStrategy.
     * @param errorHandlingStrategy the errorHandlingStrategy to set
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        httpClient.getEndpointConfiguration().setErrorHandlingStrategy(errorHandlingStrategy);
    }

    /**
     * Gets the requestMethod.
     * @return the requestMethod
     */
    public HttpMethod getRequestMethod() {
        return httpClient.getEndpointConfiguration().getRequestMethod();
    }

    /**
     * Gets the charset.
     * @return the charset
     */
    public String getCharset() {
        return httpClient.getEndpointConfiguration().getCharset();
    }

    /**
     * Gets the contentType.
     * @return the contentType
     */
    public String getContentType() {
        return httpClient.getEndpointConfiguration().getContentType();
    }

    /**
     * Gets the restTemplate.
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
        return httpClient.getEndpointConfiguration().getRestTemplate();
    }

    /**
     * Gets the endpointUriResolver.
     * @return the endpointUriResolver
     */
    public EndpointUriResolver getEndpointUriResolver() {
        return httpClient.getEndpointConfiguration().getEndpointUriResolver();
    }

    /**
     * Gets the headerMapper.
     * @return the headerMapper
     */
    public HeaderMapper<HttpHeaders> getHeaderMapper() {
        return httpClient.getEndpointConfiguration().getHeaderMapper();
    }

    /**
     * Sets the interceptors on this implementation's rest template.
     * @param interceptors the interceptors to set
     */
    public void setInterceptors(List<ClientHttpRequestInterceptor> interceptors) {
        httpClient.getEndpointConfiguration().setClientInterceptors(interceptors);
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(ReplyMessageCorrelator correlator) {
        httpClient.getEndpointConfiguration().setCorrelator(correlator);
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public ReplyMessageCorrelator getCorrelator() {
        return httpClient.getEndpointConfiguration().getCorrelator();
    }

}
