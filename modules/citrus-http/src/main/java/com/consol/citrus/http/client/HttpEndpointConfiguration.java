/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.http.client;

import com.consol.citrus.endpoint.AbstractPollableEndpointConfiguration;
import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.endpoint.resolver.EndpointUriResolver;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.message.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.*;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Http url as service destination */
    private String requestUrl;

    /** Request method */
    private HttpMethod requestMethod = HttpMethod.POST;

    /** The request charset */
    private String charset = "UTF-8";

    /** Default content type */
    private String contentType = "text/plain";

    /** The rest template */
    private RestTemplate restTemplate;

    /** Request factory */
    private ClientHttpRequestFactory requestFactory;

    /** Resolves dynamic endpoint uri */
    private EndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

    /** Header mapper */
    private HeaderMapper<HttpHeaders> headerMapper = DefaultHttpHeaderMapper.outboundMapper();

    /** The message converter */
    private HttpMessageConverter messageConverter = new HttpMessageConverter();

    /** Endpoint clientInterceptors */
    private List<ClientHttpRequestInterceptor> clientInterceptors;

    /** Should http errors be handled within endpoint consumer or simply throw exception */
    private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.PROPAGATE;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /**
     * Get the complete request URL.
     * @return the urlPath
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Set the complete request URL.
     * @param url the url to set
     */
    public void setRequestUrl(String url) {
        this.requestUrl = url;
    }

    /**
     * Sets the endpoint uri resolver.
     * @param endpointUriResolver the endpointUriResolver to set
     */
    public void setEndpointUriResolver(EndpointUriResolver endpointUriResolver) {
        this.endpointUriResolver = endpointUriResolver;
    }

    /**
     * Sets the restTemplate.
     * @param restTemplate the restTemplate to set
     */
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the requestMethod.
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(HttpMethod requestMethod) {
        this.requestMethod = requestMethod;
    }

    /**
     * Sets the charset.
     * @param charset the charset to set
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Sets the headerMapper.
     * @param headerMapper the headerMapper to set
     */
    public void setHeaderMapper(HeaderMapper<HttpHeaders> headerMapper) {
        this.headerMapper = headerMapper;
    }

    /**
     * Sets the contentType.
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the errorHandlingStrategy.
     * @return the errorHandlingStrategy
     */
    public ErrorHandlingStrategy getErrorHandlingStrategy() {
        return errorHandlingStrategy;
    }

    /**
     * Sets the errorHandlingStrategy.
     * @param errorHandlingStrategy the errorHandlingStrategy to set
     */
    public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
        this.errorHandlingStrategy = errorHandlingStrategy;
    }

    /**
     * Gets the requestMethod.
     * @return the requestMethod
     */
    public HttpMethod getRequestMethod() {
        return requestMethod;
    }

    /**
     * Gets the charset.
     * @return the charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * Gets the contentType.
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the restTemplate.
     * @return the restTemplate
     */
    public RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(getRequestFactory());
        }

        return restTemplate;
    }

    /**
     * Gets the endpointUriResolver.
     * @return the endpointUriResolver
     */
    public EndpointUriResolver getEndpointUriResolver() {
        return endpointUriResolver;
    }

    /**
     * Gets the headerMapper.
     * @return the headerMapper
     */
    public HeaderMapper<HttpHeaders> getHeaderMapper() {
        return headerMapper;
    }

    /**
     * Gets the list of endpoint clientInterceptors.
     * @return
     */
    public List<ClientHttpRequestInterceptor> getClientInterceptors() {
        return clientInterceptors;
    }

    /**
     * Sets the clientInterceptors on this implementation's rest template.
     * @param clientInterceptors the clientInterceptors to set
     */
    public void setClientInterceptors(List<ClientHttpRequestInterceptor> clientInterceptors) {
        this.clientInterceptors = clientInterceptors;
        getRestTemplate().setInterceptors(clientInterceptors);
    }

    /**
     * Set the reply message correlator.
     * @param correlator the correlator to set
     */
    public void setCorrelator(MessageCorrelator correlator) {
        this.correlator = correlator;
    }

    /**
     * Gets the correlator.
     * @return the correlator
     */
    public MessageCorrelator getCorrelator() {
        return correlator;
    }

    /**
     * Gets the client request factory.
     * @return
     */
    public ClientHttpRequestFactory getRequestFactory() {
        if (requestFactory == null) {
            requestFactory = new HttpComponentsClientHttpRequestFactory();
        }

        return requestFactory;
    }

    /**
     * Sets the client request factory.
     * @param requestFactory
     */
    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public HttpMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(HttpMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

}
