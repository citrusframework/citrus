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

package org.citrusframework.ws.client;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.AbstractEndpointBuilder;
import org.citrusframework.endpoint.resolver.EndpointUriResolver;
import org.citrusframework.message.ErrorHandlingStrategy;
import org.citrusframework.message.MessageCorrelator;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.converter.WebServiceMessageConverter;
import org.citrusframework.yaml.SchemaProperty;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;

/**
 * @since 2.5
 */
public class WebServiceClientBuilder extends AbstractEndpointBuilder<WebServiceClient> {

    /** Endpoint target */
    private final WebServiceClient endpoint = new WebServiceClient();

    private String messageFactory;
    private String webServiceTemplate;
    private String messageSender;
    private String messageConverter;
    private String correlator;
    private String endpointResolver;
    private final List<String> interceptors = new ArrayList<>();

    @Override
    public WebServiceClient build() {
        if (referenceResolver != null) {
            if (StringUtils.hasText(messageFactory)) {
                messageFactory(referenceResolver.resolve(messageFactory, WebServiceMessageFactory.class));
            }

            if (StringUtils.hasText(webServiceTemplate)) {
                webServiceTemplate(referenceResolver.resolve(webServiceTemplate, WebServiceTemplate.class));
            }

            if (StringUtils.hasText(messageSender)) {
                messageSender(referenceResolver.resolve(messageSender, WebServiceMessageSender.class));
            }

            if (StringUtils.hasText(messageConverter)) {
                messageConverter(referenceResolver.resolve(messageConverter, WebServiceMessageConverter.class));
            }

            if (StringUtils.hasText(correlator)) {
                correlator(referenceResolver.resolve(correlator, MessageCorrelator.class));
            }

            if (StringUtils.hasText(endpointResolver)) {
                endpointResolver(referenceResolver.resolve(endpointResolver, EndpointUriResolver.class));
            }

            if (!interceptors.isEmpty()) {
                interceptors(interceptors.stream()
                        .map(interceptor -> referenceResolver.resolve(interceptor, ClientInterceptor.class))
                        .toList());
            }
        }

        return super.build();
    }

    @Override
    protected WebServiceClient getEndpoint() {
        return endpoint;
    }

    /**
     * Sets the defaultUri property.
     */
    public WebServiceClientBuilder defaultUri(String uri) {
        endpoint.getEndpointConfiguration().setDefaultUri(uri);
        return this;
    }

    @SchemaProperty(description = "Sets the SOAP Web Service server url.")
    public void setDefaultUri(String defaultUri) {
        defaultUri(defaultUri);
    }

    /**
     * Sets the messageFactory property.
     */
    public WebServiceClientBuilder messageFactory(WebServiceMessageFactory messageFactory) {
        endpoint.getEndpointConfiguration().setMessageFactory(messageFactory);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom message factory.")
    public void setMessageFactory(String messageFactory) {
        this.messageFactory = messageFactory;
    }

    /**
     * Sets the keepSoapEnvelope property.
     */
    public WebServiceClientBuilder keepSoapEnvelope(boolean flag) {
        endpoint.getEndpointConfiguration().setKeepSoapEnvelope(flag);
        return this;
    }

    @SchemaProperty(advanced = true, description = "When enabled the client does not remove the SOAP envelope before processing the message.")
    public void setKeepSoapEnvelope(boolean flag) {
        keepSoapEnvelope(flag);
    }

    /**
     * Sets the web service template.
     */
    public WebServiceClientBuilder webServiceTemplate(WebServiceTemplate webServiceTemplate) {
        endpoint.getEndpointConfiguration().setWebServiceTemplate(webServiceTemplate);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets custom web service template reference.")
    public void setWebServiceTemplate(String webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    /**
     * Sets the message sender.
     */
    public WebServiceClientBuilder messageSender(WebServiceMessageSender messageSender) {
        endpoint.getEndpointConfiguration().setMessageSender(messageSender);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom message sender implementation.")
    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    /**
     * Sets the message converter.
     */
    public WebServiceClientBuilder messageConverter(WebServiceMessageConverter messageConverter) {
        endpoint.getEndpointConfiguration().setMessageConverter(messageConverter);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets a custom message converter implementation.")
    public void setMessageConverter(String messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     * Sets the interceptor.
     */
    public WebServiceClientBuilder interceptor(ClientInterceptor interceptor) {
        endpoint.getEndpointConfiguration().setInterceptor(interceptor);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets a custom client interceptor."
    )
    public void setInterceptor(String interceptor) {
        this.interceptors.add(interceptor);
    }
    /**
     * Sets the interceptors.
     */
    public WebServiceClientBuilder interceptors(List<ClientInterceptor> interceptors) {
        endpoint.getEndpointConfiguration().setInterceptors(interceptors);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:intercept") },
            description = "Sets client interceptors."
    )
    public void setInterceptors(List<String> interceptors) {
        this.interceptors.addAll(interceptors);
    }

    /**
     * Sets the message correlator.
     */
    public WebServiceClientBuilder correlator(MessageCorrelator correlator) {
        endpoint.getEndpointConfiguration().setCorrelator(correlator);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the message correlator.")
    public void setCorrelator(String correlator) {
        this.correlator = correlator;
    }

    /**
     * Sets the endpoint resolver.
     */
    public WebServiceClientBuilder endpointResolver(EndpointUriResolver resolver) {
        endpoint.getEndpointConfiguration().setEndpointResolver(resolver);
        return this;
    }

    @SchemaProperty(advanced = true, description = "Sets the endpoint resolver.")
    public void setEndpointResolver(String resolver) {
        this.endpointResolver = resolver;
    }

    /**
     * Sets the fault handling strategy.
     */
    public WebServiceClientBuilder faultStrategy(ErrorHandlingStrategy faultStrategy) {
        endpoint.getEndpointConfiguration().setErrorHandlingStrategy(faultStrategy);
        return this;
    }

    @SchemaProperty(
            metadata = { @SchemaProperty.MetaData(key = "$comment", value = "group:errorHandler") },
            description = "Sets the error handling strategy."
    )
    public void setFaultStrategy(ErrorHandlingStrategy faultStrategy) {
        faultStrategy(faultStrategy);
    }

    /**
     * Sets the polling interval.
     */
    public WebServiceClientBuilder pollingInterval(int pollingInterval) {
        endpoint.getEndpointConfiguration().setPollingInterval(pollingInterval);
        return this;
    }

    @SchemaProperty(description = "Sets the polling interval when consuming messages.")
    public void setPollingInterval(int pollingInterval) {
        pollingInterval(pollingInterval);
    }

    /**
     * Sets the default timeout.
     */
    public WebServiceClientBuilder timeout(long timeout) {
        endpoint.getEndpointConfiguration().setTimeout(timeout);
        return this;
    }

    @SchemaProperty(description = "Sets the receive timeout when the client waits for response messages.", defaultValue = "5000")
    public void setTimeout(long timeout) {
        timeout(timeout);
    }
}
