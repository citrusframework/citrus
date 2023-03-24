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

package org.citrusframework.ws.client;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;

import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class WebServiceClientBuilder extends AbstractEndpointBuilder<WebServiceClient> {

    /** Endpoint target */
    private WebServiceClient endpoint = new WebServiceClient();

    @Override
    protected WebServiceClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the defaultUri property.
     * @param uri
     * @return
     */
    public WebServiceClientBuilder defaultUri(String uri) {
        endpoint.getEndpointConfiguration().setDefaultUri(uri);
        return this;
    }

    /**
     * Sets the messageFactory property.
     * @param messageFactory
     * @return
     */
    public WebServiceClientBuilder messageFactory(WebServiceMessageFactory messageFactory) {
        endpoint.getEndpointConfiguration().setMessageFactory(messageFactory);
        return this;
    }

    /**
     * Sets the keepSoapEnvelope property.
     * @param flag
     * @return
     */
    public WebServiceClientBuilder keepSoapEnvelope(boolean flag) {
        endpoint.getEndpointConfiguration().setKeepSoapEnvelope(flag);
        return this;
    }

    /**
     * Sets the web service template.
     * @param webServiceTemplate
     * @return
     */
    public WebServiceClientBuilder webServiceTemplate(WebServiceTemplate webServiceTemplate) {
        endpoint.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        return this;
    }

    /**
     * Sets the message sender.
     * @param messageSender
     * @return
     */
    public WebServiceClientBuilder messageSender(WebServiceMessageSender messageSender) {
        endpoint.getEndpointConfiguration().setMessageSender(messageSender);
        return this;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     * @return
     */
    public WebServiceClientBuilder messageConverter(WebServiceMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    /**
     * Sets the interceptor.
     * @param interceptor
     * @return
     */
    public WebServiceClientBuilder interceptor(ClientInterceptor interceptor) {
        endpoint.getEndpointConfiguration().setInterceptor(interceptor);
        return this;
    }

    /**
     * Sets the interceptors.
     * @param interceptors
     * @return
     */
    public WebServiceClientBuilder interceptors(List<ClientInterceptor> interceptors) {
        endpoint.getEndpointConfiguration().setInterceptors(interceptors);
        return this;
    }

    /**
     * Sets the message correlator.
     * @param correlator
     * @return
     */
    public WebServiceClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    /**
     * Sets the endpoint resolver.
     * @param resolver
     * @return
     */
    public WebServiceClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointResolver(resolver);
        return this;
    }

    /**
     * Sets the fault handling strategy.
     * @param faultStrategy
     * @return
     */
    public WebServiceClientBuilder faultStrategy(ErrorHandlingStrategy faultStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(faultStrategy);
        return this;
    }

    /**
     * Sets the polling interval.
     * @param pollingInterval
     * @return
     */
    public WebServiceClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    /**
     * Sets the default timeout.
     * @param timeout
     * @return
     */
    public WebServiceClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }
}
