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

package org.citrusframework.http.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.citrusframework.endpoint.AbstractPollableEndpointConfiguration;
import org.citrusframework.endpoint.resolver.DynamicEndpointUriResolver;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.http.interceptor.LoggingClientInterceptor;
import org.citrusframework.http.message.HttpMessageConverter;
import org.citrusframework.message.DefaultMessageCorrelator;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class HttpEndpointConfiguration extends AbstractPollableEndpointConfiguration {

    /** Http url as service destination */
    private String requestUrl;

    /** Request method */
    private RequestMethod requestMethod = RequestMethod.POST;

    /** The request charset */
    private String charset = "UTF-8";

    /** Default content type */
    private String contentType = "text/plain";

    /** The rest template */
    private RestTemplate restTemplate;

    /** Http client builder */
    private HttpClientBuilder httpClient;

    /** Request factory */
    private ClientHttpRequestFactory requestFactory;

    /** Resolves dynamic endpoint uri */
    private EndpointUriResolver endpointUriResolver = new DynamicEndpointUriResolver();

    /** Header mapper */
    private HeaderMapper<HttpHeaders> headerMapper = DefaultHttpHeaderMapper.outboundMapper();

    /** The message converter */
    private HttpMessageConverter messageConverter = new HttpMessageConverter();

    /** Endpoint clientInterceptors */
    private List<ClientHttpRequestInterceptor> clientInterceptors = new ArrayList<>();

    /** Should http errors be handled within endpoint consumer or simply throw exception */
    private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.PROPAGATE;

    /** Response error handler */
    private ResponseErrorHandler errorHandler;

    /** Reply message correlator */
    private MessageCorrelator correlator = new DefaultMessageCorrelator();

    /** Auto add default accept header with os supported content-types */
    private boolean defaultAcceptHeader = true;

    /** Should handle http attributes */
    private boolean handleAttributeHeaders = false;

    /** Should handle http cookies */
    private boolean handleCookies = false;

    /** Default status code returned by http server */
    private int defaultStatusCode = HttpStatus.OK.value();

    /** List of media types that should be handled with binary content processing */
    private List<MediaType> binaryMediaTypes = Arrays.asList(MediaType.APPLICATION_OCTET_STREAM,
                                                                MediaType.APPLICATION_PDF,
                                                                MediaType.IMAGE_GIF,
                                                                MediaType.IMAGE_JPEG,
                                                                MediaType.IMAGE_PNG,
                                                                MediaType.valueOf("application/zip"));

    /**
     * Default constructor initializes with default logging interceptor.
     */
    public HttpEndpointConfiguration() {
        clientInterceptors.add(new LoggingClientInterceptor());
    }

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
        clientInterceptors.addAll(restTemplate.getInterceptors());
        restTemplate.setInterceptors(clientInterceptors);
        this.restTemplate = restTemplate;
    }

    /**
     * Sets the requestMethod.
     * @param requestMethod the requestMethod to set
     */
    public void setRequestMethod(RequestMethod requestMethod) {
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
    public RequestMethod getRequestMethod() {
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
            restTemplate.setInterceptors(clientInterceptors);
        }

        restTemplate.setRequestFactory(getRequestFactory());
        restTemplate.setErrorHandler(getErrorHandler());

        if (!defaultAcceptHeader) {
            restTemplate.getMessageConverters().stream()
                    .filter(StringHttpMessageConverter.class::isInstance)
                    .map(StringHttpMessageConverter.class::cast)
                    .forEach(converter -> converter.setWriteAcceptCharset(defaultAcceptHeader));
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
            requestFactory = new HttpComponentsClientHttpRequestFactory(getHttpClient().build());
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

    public HttpClientBuilder getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create().useSystemProperties();
        }

        return httpClient;
    }

    public void setHttpClient(HttpClientBuilder httpClient) {
        this.httpClient = httpClient;
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

    /**
     * Sets the defaultAcceptHeader property.
     *
     * @param defaultAcceptHeader
     */
    public void setDefaultAcceptHeader(boolean defaultAcceptHeader) {
        this.defaultAcceptHeader = defaultAcceptHeader;
    }

    /**
     * Gets the value of the defaultAcceptHeader property.
     *
     * @return the defaultAcceptHeader
     */
    public boolean isDefaultAcceptHeader() {
        return defaultAcceptHeader;
    }

    /**
     * Gets the handleAttributeHeaders.
     *
     * @return
     */
    public boolean isHandleAttributeHeaders() {
        return handleAttributeHeaders;
    }

    /**
     * Sets the handleAttributeHeaders.
     *
     * @param handleAttributeHeaders
     */
    public void setHandleAttributeHeaders(boolean handleAttributeHeaders) {
        this.handleAttributeHeaders = handleAttributeHeaders;
    }

    /**
     * Gets the handleCookies.
     *
     * @return
     */
    public boolean isHandleCookies() {
        return handleCookies;
    }

    /**
     * Sets the handleCookies.
     *
     * @param handleCookies
     */
    public void setHandleCookies(boolean handleCookies) {
        this.handleCookies = handleCookies;
    }

    /**
     * Gets the errorHandler.
     *
     * @return
     */
    public ResponseErrorHandler getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = new HttpResponseErrorHandler(errorHandlingStrategy);
        }

        return errorHandler;
    }

    /**
     * Sets the errorHandler.
     *
     * @param errorHandler
     */
    public void setErrorHandler(ResponseErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Gets the defaultStatusCode.
     *
     * @return
     */
    public int getDefaultStatusCode() {
        return defaultStatusCode;
    }

    /**
     * Sets the defaultStatusCode.
     *
     * @param defaultStatusCode
     */
    public void setDefaultStatusCode(int defaultStatusCode) {
        this.defaultStatusCode = defaultStatusCode;
    }

    /**
     * Gets the binaryMediaTypes.
     *
     * @return
     */
    public List<MediaType> getBinaryMediaTypes() {
        return binaryMediaTypes;
    }

    /**
     * Sets the binaryMediaTypes.
     *
     * @param binaryMediaTypes
     */
    public void setBinaryMediaTypes(List<MediaType> binaryMediaTypes) {
        this.binaryMediaTypes = binaryMediaTypes;
    }
}
