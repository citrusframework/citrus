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

package org.citrusframework.knative.actions.eventing;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpServerActionBuilder;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.ce.CloudEvent;
import org.citrusframework.knative.ce.CloudEventMessage;
import org.citrusframework.knative.ce.CloudEventSupport;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * Receives CloudEvent event from Knative message broker.
 * Uses Knative trigger and service binding to receive the CloudEvent data.
 */
public class ReceiveEventAction extends AbstractKnativeAction {

    private final CloudEventMessage message;
    private final HttpServer httpServer;

    public ReceiveEventAction(Builder builder) {
        super("receive-event", builder);

        this.message = builder.message;
        this.httpServer = builder.httpServer;
    }

    @Override
    public void doExecute(TestContext context) {
        receiveEvent(message, context);
    }

    /**
     * Receives event from given service.
     * @param request
     */
    public void receiveEvent(HttpMessage request, TestContext context) {
        if (!httpServer.isRunning()) {
            httpServer.start();
        }

        HttpServerActionBuilder.HttpServerReceiveActionBuilder receiveBuilder = http().server(httpServer).receive();
        HttpServerRequestActionBuilder.HttpMessageBuilderSupport requestBuilder;

        if (request.getRequestMethod() == null || request.getRequestMethod().equals(RequestMethod.POST)) {
            requestBuilder = receiveBuilder.post().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.GET)) {
            requestBuilder = receiveBuilder.get().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PUT)) {
            requestBuilder = receiveBuilder.put().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.DELETE)) {
            requestBuilder = receiveBuilder.delete().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.HEAD)) {
            requestBuilder = receiveBuilder.head().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.TRACE)) {
            requestBuilder = receiveBuilder.trace().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.PATCH)) {
            requestBuilder = receiveBuilder.patch().message(request);
        } else if (request.getRequestMethod().equals(RequestMethod.OPTIONS)) {
            requestBuilder = receiveBuilder.options().message(request);
        } else {
            requestBuilder = receiveBuilder.post().message(request);
        }

        requestBuilder.headerNameIgnoreCase(true);
        requestBuilder.type(message.getType());
        requestBuilder.build().execute(context);

        http().server(httpServer)
                .send()
                .response(HttpStatus.ACCEPTED);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<ReceiveEventAction, Builder> {

        private String serviceName = KnativeSettings.getServiceName();
        private int servicePort = KnativeSettings.getServicePort();
        private HttpServer httpServer;
        private CloudEventMessage message;
        private String eventData;
        private final Map<String, String> ceAttributes = new HashMap<>();
        private long timeout = KnativeSettings.getEventProducerTimeout();

        public Builder service(String serviceName, int servicePort) {
            this.serviceName = serviceName;
            this.servicePort = servicePort;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder servicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder event(CloudEventMessage message) {
            this.message = message;
            return this;
        }

        public Builder event(CloudEvent event) {
            return event(CloudEventMessage.fromEvent(event));
        }

        public Builder eventData(String eventData) {
            this.eventData = eventData;
            return this;
        }

        public Builder attributes(Map<String, String> ceAttributes) {
            this.ceAttributes.putAll(ceAttributes);
            return this;
        }

        public Builder attribute(String name, String value) {
            this.ceAttributes.put(name, value);
            return this;
        }

        public Builder server(HttpServer httpServer) {
            this.httpServer = httpServer;
            return this;
        }

        @Override
        public ReceiveEventAction doBuild() {
            if (message == null) {
                message = CloudEventSupport.createEventMessage(eventData, ceAttributes);
            }

            if (httpServer == null) {
                if (referenceResolver != null && referenceResolver.isResolvable(serviceName, HttpServer.class)) {
                    httpServer = referenceResolver.resolve(serviceName, HttpServer.class);
                } else {
                    httpServer = new HttpServerBuilder()
                            .autoStart(true)
                            .timeout(timeout)
                            .port(servicePort)
                            .name(serviceName)
                            .build();

                    httpServer.initialize();
                }
            }

            if (referenceResolver != null && !referenceResolver.isResolvable(serviceName, HttpServer.class)) {
                referenceResolver.bind(serviceName, httpServer);
            }

            return new ReceiveEventAction(this);
        }
    }
}
