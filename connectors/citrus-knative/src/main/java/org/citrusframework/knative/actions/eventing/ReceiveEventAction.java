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

import org.citrusframework.actions.knative.KnativeEventReceiveActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.actions.HttpActionBuilder;
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
import org.citrusframework.message.Message;
import org.citrusframework.util.PropertyUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Receives CloudEvent event from Knative message broker.
 * Uses Knative trigger and service binding to receive the CloudEvent data.
 */
public class ReceiveEventAction extends AbstractKnativeAction {

    private final Message message;
    private final HttpServer httpServer;

    private final HttpActionBuilder http = new HttpActionBuilder();

    public ReceiveEventAction(Builder builder) {
        super("receive-event", builder);

        this.message = builder.message;
        this.httpServer = builder.httpServer;
    }

    @Override
    public void doExecute(TestContext context) {
        if (message instanceof CloudEventMessage ceMessage) {
            receiveEvent(ceMessage, context);
        } else {
            receiveEvent(CloudEventSupport.createEventMessage(message.getPayload(String.class), message.getHeaders()), context);
        }
    }

    /**
     * Receives event from given service.
     * @param request
     */
    public void receiveEvent(HttpMessage request, TestContext context) {
        if (!httpServer.isRunning()) {
            httpServer.start();
        }

        HttpServerActionBuilder.HttpServerReceiveActionBuilder receiveBuilder = http.server(httpServer).receive();
        HttpServerRequestActionBuilder requestBuilder;

        if (request.getRequestMethod() == null || request.getRequestMethod().equals(RequestMethod.POST)) {
            requestBuilder = receiveBuilder.post();
        } else if (request.getRequestMethod().equals(RequestMethod.GET)) {
            requestBuilder = receiveBuilder.get();
        } else if (request.getRequestMethod().equals(RequestMethod.PUT)) {
            requestBuilder = receiveBuilder.put();
        } else if (request.getRequestMethod().equals(RequestMethod.DELETE)) {
            requestBuilder = receiveBuilder.delete();
        } else if (request.getRequestMethod().equals(RequestMethod.HEAD)) {
            requestBuilder = receiveBuilder.head();
        } else if (request.getRequestMethod().equals(RequestMethod.TRACE)) {
            requestBuilder = receiveBuilder.trace();
        } else if (request.getRequestMethod().equals(RequestMethod.PATCH)) {
            requestBuilder = receiveBuilder.patch();
        } else if (request.getRequestMethod().equals(RequestMethod.OPTIONS)) {
            requestBuilder = receiveBuilder.options();
        } else {
            requestBuilder = receiveBuilder.post();
        }

        requestBuilder
                .message(request)
                .headerNameIgnoreCase(true)
                .type(message.getType())
                .build().execute(context);

        http.server(httpServer)
                .send()
                .response(HttpStatus.ACCEPTED)
                .build().execute(context);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<ReceiveEventAction, Builder>
            implements KnativeEventReceiveActionBuilder<ReceiveEventAction, Builder> {

        private String serviceName = KnativeSettings.getServiceName();
        private int servicePort = KnativeSettings.getServicePort();
        private HttpServer httpServer;
        private Message message;
        private String eventData;
        private final Map<String, Object> ceAttributes = new HashMap<>();
        private long timeout = KnativeSettings.getEventProducerTimeout();

        @Override
        public Builder service(String serviceName, int servicePort) {
            this.serviceName = serviceName;
            this.servicePort = servicePort;
            return this;
        }

        @Override
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public Builder servicePort(int servicePort) {
            this.servicePort = servicePort;
            return this;
        }

        @Override
        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        @Override
        public Builder event(Message message) {
            this.message = message;
            return this;
        }

        public Builder event(CloudEvent event) {
            return event(CloudEventMessage.fromEvent(event));
        }

        @Override
        public Builder event(Object event) {
            if (event instanceof CloudEventMessage cloudEventMessage) {
                return event(cloudEventMessage);
            } else if (event instanceof CloudEvent cloudEvent) {
                return event(CloudEventMessage.fromEvent(cloudEvent));
            } else {
                throw new CitrusRuntimeException("Unsupported event type: " + event.getClass().getName());
            }
        }

        @Override
        public Builder eventData(String eventData) {
            this.eventData = eventData;
            return this;
        }

        @Override
        public Builder attributes(Map<String, Object> ceAttributes) {
            this.ceAttributes.putAll(ceAttributes);
            return this;
        }

        @Override
        public Builder attribute(String name, Object value) {
            this.ceAttributes.put(name, value);
            return this;
        }

        @Override
        public Builder server(Endpoint endpoint) {
            if (endpoint instanceof HttpServer server) {
                return server(server);
            } else {
                throw new CitrusRuntimeException("Unsupported server endpoint type: " + endpoint.getClass().getName());
            }
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
                            .referenceResolver(referenceResolver)
                            .build();

                    httpServer.initialize();
                    PropertyUtils.configure(serviceName, httpServer, referenceResolver);
                }
            }

            if (referenceResolver != null && !referenceResolver.isResolvable(serviceName, HttpServer.class)) {
                referenceResolver.bind(serviceName, httpServer);
            }

            return new ReceiveEventAction(this);
        }
    }
}
