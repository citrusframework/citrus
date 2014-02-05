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

package com.consol.citrus.http.servlet;

import com.consol.citrus.adapter.handler.*;
import com.consol.citrus.channel.ChannelEndpointConfiguration;
import com.consol.citrus.http.controller.HttpMessageController;
import com.consol.citrus.http.interceptor.DelegatingHandlerInterceptor;
import com.consol.citrus.http.interceptor.MappedInterceptorAdapter;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.jms.JmsEndpointConfiguration;
import com.consol.citrus.jms.JmsSyncEndpointConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import org.springframework.web.util.UrlPathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Citrus dispatcher servlet extends Spring's message dispatcher servlet and just
 * adds optional configuration settings for default mapping strategies and so on.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class CitrusDispatcherServlet extends DispatcherServlet {

    /** Http server hosting the servlet */
    private HttpServer httpServer;

    /** Default bean names used in default configuration */
    private static final String HANDLER_INTERCEPTOR_BEAN_NAME = "citrusHandlerInterceptor";
    private static final String MESSAGE_CONTROLLER_BEAN_NAME = "citrusHttpMessageController";

    /**
     * Default constructor using http server instance that
     * holds this servlet.
     * @param httpServer
     */
    public CitrusDispatcherServlet(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    protected void initStrategies(ApplicationContext context) {
        super.initStrategies(context);

        configureHandlerInterceptor(context);
        configureMessageController(context);
    }

    /**
     * Post process handler interceptors.
     * @param context
     */
    protected void configureHandlerInterceptor(ApplicationContext context) {
        if (context.containsBean(HANDLER_INTERCEPTOR_BEAN_NAME)) {
            DelegatingHandlerInterceptor handlerInterceptor = context.getBean(HANDLER_INTERCEPTOR_BEAN_NAME, DelegatingHandlerInterceptor.class);
            handlerInterceptor.setInterceptors(adaptInterceptors(httpServer.getInterceptors()));
        }
    }

    /**
     * Post process message controller.
     * @param context
     */
    protected void configureMessageController(ApplicationContext context) {
        if (context.containsBean(MESSAGE_CONTROLLER_BEAN_NAME)) {
            HttpMessageController messageController = context.getBean(MESSAGE_CONTROLLER_BEAN_NAME, HttpMessageController.class);

            if (httpServer.getEndpointConfiguration() == null) {
                messageController.setMessageHandler(new EmptyResponseProducingMessageHandler());
            } else if (httpServer.getEndpointConfiguration() instanceof ChannelEndpointConfiguration) {
                ChannelEndpointConfiguration channelEndpointConfiguration = (ChannelEndpointConfiguration) httpServer.getEndpointConfiguration();
                MessageChannelConnectingMessageHandler messageHandler = new MessageChannelConnectingMessageHandler();
                messageHandler.setBeanFactory(context);
                messageHandler.setReplyTimeout(channelEndpointConfiguration.getTimeout());

                if (channelEndpointConfiguration.getMessagingTemplate() != null) {
                    messageHandler.setMessagingTemplate(channelEndpointConfiguration.getMessagingTemplate());
                }

                messageHandler.setChannel(channelEndpointConfiguration.getChannel());
                messageHandler.setChannelName(channelEndpointConfiguration.getChannelName());

                messageController.setMessageHandler(messageHandler);
            } else if (httpServer.getEndpointConfiguration() instanceof JmsEndpointConfiguration) {
                JmsEndpointConfiguration jmsEndpointConfiguration = (JmsEndpointConfiguration) httpServer.getEndpointConfiguration();
                JmsConnectingMessageHandler messageHandler = new JmsConnectingMessageHandler();
                messageHandler.setReplyTimeout(jmsEndpointConfiguration.getTimeout());

                if (jmsEndpointConfiguration.getConnectionFactory() != null) {
                    messageHandler.setConnectionFactory(jmsEndpointConfiguration.getConnectionFactory());
                }

                messageHandler.setDestination(jmsEndpointConfiguration.getDestination());
                messageHandler.setDestinationName(jmsEndpointConfiguration.getDestinationName());

                if (jmsEndpointConfiguration instanceof JmsSyncEndpointConfiguration) {
                    messageHandler.setReplyDestination(((JmsSyncEndpointConfiguration) jmsEndpointConfiguration).getReplyDestination());
                    messageHandler.setReplyDestinationName(((JmsSyncEndpointConfiguration) jmsEndpointConfiguration).getReplyDestinationName());
                }

                messageController.setMessageHandler(messageHandler);
            }
        }
    }

    /**
     * Adapts object list to handler interceptors.
     * @param interceptors
     * @return
     */
    private List<HandlerInterceptor> adaptInterceptors(List<Object> interceptors) {
        List<HandlerInterceptor> handlerInterceptors = new ArrayList<HandlerInterceptor>();

        for (Object interceptor : interceptors) {
            if (interceptor instanceof HandlerInterceptor) {
                handlerInterceptors.add((HandlerInterceptor) interceptor);
            } else if (interceptor instanceof WebRequestInterceptor) {
                handlerInterceptors.add(new WebRequestHandlerInterceptorAdapter((WebRequestInterceptor) interceptor));
            } else if (interceptor instanceof MappedInterceptor) {
                handlerInterceptors.add(new MappedInterceptorAdapter((MappedInterceptor)interceptor,
                        new UrlPathHelper(), new AntPathMatcher()));
            }
        }

        return handlerInterceptors;
    }
}
