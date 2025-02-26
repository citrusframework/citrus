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
import org.citrusframework.agent.connector.UnitTestSupport;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.adapter.StaticEndpointAdapter;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.message.Message;
import org.citrusframework.util.SocketUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AgentConnectActionTest extends UnitTestSupport {

    @Test
    public void shouldVerifyConnection() {
        int randomPort = SocketUtils.findAvailableTcpPort();
        HttpServer agentServer = new HttpServerBuilder()
                .port(randomPort)
                .endpointAdapter(healthEndpointAdapter())
                .build();

        try {
            agentServer.start();

            AgentConnectAction action = new AgentActionBuilder()
                    .connect()
                    .port(randomPort)
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    @Test
    public void shouldLookupHttpClient() {
        int randomPort = SocketUtils.findAvailableTcpPort();
        HttpServer agentServer = new HttpServerBuilder()
                .port(randomPort)
                .endpointAdapter(healthEndpointAdapter())
                .build();

        context.getReferenceResolver().bind("citrus-agent.client", new HttpClientBuilder()
                .requestUrl("http://localhost:%d".formatted(randomPort))
                .build());

        try {
            agentServer.start();

            AgentConnectAction action = new AgentActionBuilder()
                    .connect()
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    @Test
    public void shouldVerifyConnectionWithDefaults() {
        HttpServer agentServer = new HttpServerBuilder()
                .port(CitrusAgentSettings.getAgentServerPort())
                .endpointAdapter(healthEndpointAdapter())
                .build();

        try {
            agentServer.start();

            AgentConnectAction action = new AgentActionBuilder()
                    .connect()
                    .build();

            action.execute(context);
        } finally {
            agentServer.stop();
        }
    }

    private EndpointAdapter healthEndpointAdapter() {
        return new StaticEndpointAdapter() {
            @Override
            protected Message handleMessageInternal(Message message) {
                if (message instanceof HttpMessage httpRequest) {
                    Assert.assertEquals(httpRequest.getPath(), "/health");
                    Assert.assertEquals(httpRequest.getRequestMethod(), RequestMethod.GET);
                    return new HttpMessage("""
                            { "status": "UP" }
                            """).status(HttpStatus.OK);
                }

                return new HttpMessage().status(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
}
