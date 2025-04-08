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

package org.citrusframework.agent.connector.actions;

import org.citrusframework.agent.connector.CitrusAgentSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.message.Message;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.ResourceAccessException;

public class AgentConnectAction extends AbstractAgentAction {

    private final String url;

    public AgentConnectAction(Builder builder) {
        super("connect", builder);

        this.url = builder.url;
    }

    @Override
    public void doExecute(TestContext context) {
        String agent = context.replaceDynamicContentInString(agentName);
        logger.info("Connecting to Citrus agent '%s'".formatted(agent));

        String clientName = agent + ".client";
        HttpClient httpClient = resolveHttpClient(clientName, context);
        verifyConnection(agent, httpClient, context);

        logger.info("Successfully connected to Citrus agent '%s'".formatted(agent));
    }

    private HttpClient resolveHttpClient(String clientName, TestContext context) {
        HttpClient httpClient;
        if (context.getReferenceResolver().isResolvable(clientName, HttpClient.class)) {
            httpClient = context.getReferenceResolver().resolve(clientName, HttpClient.class);
        } else {
            httpClient = new HttpClientBuilder()
                    .requestUrl(url)
                    .build();
            context.getReferenceResolver().bind(clientName, httpClient);
        }

        return httpClient;
    }

    private void verifyConnection(String agent, HttpClient httpClient, TestContext context) {
        try {
            httpClient.send(new HttpMessage().method(HttpMethod.GET).path("/health"), context);
            Message response = httpClient.receive(context);
            HttpMessage httpResponse;
            if (response instanceof HttpMessage) {
                httpResponse = (HttpMessage) response;
            } else {
                httpResponse = new HttpMessage(response);
            }

            if (!httpResponse.getStatusCode().is2xxSuccessful()) {
                throw new CitrusRuntimeException(("Failed to verify connection to Citrus agent server '%s', " +
                        "expected 2xx success status code, but was %d %s")
                        .formatted(agent, httpResponse.getStatusCode().value(), httpResponse.getReasonPhrase()));
            }
        } catch (MessageTimeoutException | ResourceAccessException e) {
            throw new CitrusRuntimeException("Failed to verify connection to Citrus agent server '%s' - %s".formatted(agent, e.getClass().getSimpleName()), e);
        }
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractAgentAction.Builder<AgentConnectAction, Builder> {

        private int port = CitrusAgentSettings.getAgentServerPort();
        private String url;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        @Override
        public AgentConnectAction build() {
            if (url == null) {
                url = "http://localhost:%d".formatted(port);
            }
            return new AgentConnectAction(this);
        }
    }
}
