/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.ws.servlet;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.client.WebServiceEndpointConfiguration;
import org.citrusframework.ws.interceptor.DelegatingEndpointInterceptor;
import org.citrusframework.ws.server.WebServiceEndpoint;
import org.citrusframework.ws.server.WebServiceServer;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

/**
 * Citrus message dispatcher servlet extends Spring's message dispatcher servlet and just
 * adds optional configuration settings for default mapping strategies, interceptors and so on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusMessageDispatcherServlet extends MessageDispatcherServlet {
    /** Soap web server hosting the servlet */
    private WebServiceServer webServiceServer;

    /** Default bean names used in default configuration */
    private static final String ENDPOINT_INTERCEPTOR_BEAN_NAME = "citrusEndpointInterceptor";
    private static final String MESSAGE_ENDPOINT_BEAN_NAME = "citrusWsEndpoint";

    /**
     * Default constructor using http server instance that
     * holds this servlet.
     * @param webServiceServer
     */
    public CitrusMessageDispatcherServlet(WebServiceServer webServiceServer) {
        this.webServiceServer = webServiceServer;

        //use this message factory bean name - users can specify custom factories for SOAP 1.2 for instance
        setMessageFactoryBeanName(webServiceServer.getMessageFactoryName());
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);

        configureHandlerInterceptor(context);
        configureMessageEndpoint(context);
    }

    /**
     * Post process handler interceptors.
     * @param context
     */
    protected void configureHandlerInterceptor(ApplicationContext context) {
        if (context.containsBean(ENDPOINT_INTERCEPTOR_BEAN_NAME)) {
            DelegatingEndpointInterceptor endpointInterceptor = context.getBean(ENDPOINT_INTERCEPTOR_BEAN_NAME, DelegatingEndpointInterceptor.class);
            endpointInterceptor.setInterceptors(adaptInterceptors(webServiceServer.getInterceptors()));
        }
    }

    /**
     * Post process endpoint.
     * @param context
     */
    protected void configureMessageEndpoint(ApplicationContext context) {
        if (context.containsBean(MESSAGE_ENDPOINT_BEAN_NAME)) {
            WebServiceEndpoint messageEndpoint = context.getBean(MESSAGE_ENDPOINT_BEAN_NAME, WebServiceEndpoint.class);

            EndpointAdapter endpointAdapter = webServiceServer.getEndpointAdapter();
            if (endpointAdapter != null) {
                messageEndpoint.setEndpointAdapter(endpointAdapter);
            }

            WebServiceEndpointConfiguration endpointConfiguration = new WebServiceEndpointConfiguration();
            endpointConfiguration.setHandleMimeHeaders(webServiceServer.isHandleMimeHeaders());
            endpointConfiguration.setHandleAttributeHeaders(webServiceServer.isHandleAttributeHeaders());
            endpointConfiguration.setKeepSoapEnvelope(webServiceServer.isKeepSoapEnvelope());
            endpointConfiguration.setMessageConverter(webServiceServer.getMessageConverter());
            messageEndpoint.setEndpointConfiguration(endpointConfiguration);

            if (StringUtils.hasText(webServiceServer.getSoapHeaderNamespace())) {
                messageEndpoint.setDefaultNamespaceUri(webServiceServer.getSoapHeaderNamespace());
            }

            if (StringUtils.hasText(webServiceServer.getSoapHeaderPrefix())) {
                messageEndpoint.setDefaultPrefix(webServiceServer.getSoapHeaderPrefix());
            }
        }
    }

    /**
     * Adapts object list to endpoint interceptors.
     * @param interceptors
     * @return
     */
    private List<EndpointInterceptor> adaptInterceptors(List<Object> interceptors) {
        List<EndpointInterceptor> endpointInterceptors = new ArrayList<EndpointInterceptor>();

        if (interceptors != null) {
            for (Object interceptor : interceptors) {
                if (interceptor instanceof EndpointInterceptor) {
                    endpointInterceptors.add((EndpointInterceptor) interceptor);
                }
            }
        }

        return endpointInterceptors;
    }
}
