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
import java.util.Objects;
import java.util.UUID;

import org.citrusframework.context.TestContext;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.KnativeSupport;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.ce.CloudEvent;
import org.citrusframework.knative.ce.CloudEventMessage;
import org.citrusframework.knative.ce.CloudEventSupport;
import org.citrusframework.spi.ReferenceResolverAware;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import static org.citrusframework.http.actions.HttpActionBuilder.http;

/**
 * Send CloudEvent event to Knative message broker. Uses the Http transport to send the CloudEvent data.
 */
public class SendEventAction extends AbstractKnativeAction {

    private final String brokerUrl;
    private final HttpClient httpClient;
    private final long timeout;
    private final CloudEventMessage message;
    private final boolean forkMode;

    public SendEventAction(Builder builder) {
        super("send-event", builder);

        this.brokerUrl = builder.brokerUrl;
        this.httpClient = builder.httpClient;
        this.message = builder.message;
        this.timeout = builder.timeout;
        this.forkMode = builder.forkMode;
    }

    @Override
    public void doExecute(TestContext context) {
        sendEvent(message, context);
    }

    /**
     * Sends event request as Http request and verify accepted response.
     * @param request
     * @param context
     */
    private void sendEvent(CloudEventMessage request, TestContext context) {
        if (Objects.isNull(request.getContentType())) {
            request.contentType(MediaType.APPLICATION_JSON_VALUE);
        }

        if (request.getEventId() == null) {
            request.eventId(UUID.randomUUID().toString());
        }

        if (request.getEventType() == null) {
            request.eventType("org.citrusframework.event.test");
        }

        if (request.getSource() == null) {
            request.source("citrus-test");
        }

        request.setHeader("Host", KnativeSettings.getBrokerHost());

        HttpClientRequestActionBuilder.HttpMessageBuilderSupport requestBuilder = http().client(httpClient)
                .send()
                .post()
                .message(request);

        requestBuilder.fork(forkMode);

        if (StringUtils.hasText(brokerUrl)) {
            requestBuilder.uri(brokerUrl);
        }

        requestBuilder.build().execute(context);

        if (KnativeSettings.isVerifyBrokerResponse()) {
            http().client(httpClient)
                    .receive()
                    .response(HttpStatus.valueOf(KnativeSettings.getBrokerResponseStatus()))
                    .timeout(timeout)
                    .build().execute(context);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKnativeAction.Builder<SendEventAction, Builder> implements ReferenceResolverAware {

        private String brokerUrl = KnativeSettings.getBrokerUrl();
        private String brokerName;
        private HttpClient httpClient;
        private CloudEventMessage message;
        private String eventData;
        private final Map<String, String> ceAttributes = new HashMap<>();
        private long timeout = KnativeSettings.getEventProducerTimeout();
        private boolean forkMode;

        public Builder broker(String brokerName) {
            this.brokerName = brokerName;
            return this;
        }

        public Builder brokerUrl(String brokerUrl) {
            this.brokerUrl = brokerUrl;
            return this;
        }

        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder fork(boolean enabled) {
            this.forkMode = enabled;
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

        public Builder client(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        @Override
        public SendEventAction doBuild() {
            if (message == null) {
                message = CloudEventSupport.createEventMessage(eventData, ceAttributes);
            }

            if (httpClient == null) {
                if (referenceResolver != null && brokerName != null &&
                        referenceResolver.isResolvable(brokerName, HttpClient.class)) {
                    httpClient = referenceResolver.resolve(brokerName, HttpClient.class);
                    brokerUrl = httpClient.getEndpointConfiguration().getRequestUrl();
                } else {
                    httpClient = new HttpClientBuilder()
                            .timeout(timeout)
                            .requestUrl(brokerUrl)
                            .build();

                    if (brokerUrl.startsWith("https")) {
                        httpClient.getEndpointConfiguration().setRequestFactory(KnativeSupport.sslRequestFactory());
                    }
                }
            }

            if (referenceResolver != null && brokerName != null &&
                    !referenceResolver.isResolvable(brokerName, HttpClient.class)) {
                referenceResolver.bind(brokerName, httpClient);
            }

            return new SendEventAction(this);
        }
    }
}
