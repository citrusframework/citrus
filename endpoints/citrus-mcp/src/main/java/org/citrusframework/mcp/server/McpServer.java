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

package org.citrusframework.mcp.server;

import java.util.HashSet;
import java.util.Set;

import io.quarkus.runtime.Quarkus;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import org.citrusframework.base.server.AbstractServer;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpServerConfiguration;
import org.citrusframework.mcp.model.McpTool;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McpServer extends AbstractServer {

    private static final Logger logger = LoggerFactory.getLogger(McpServer.class);

    private final Set<McpTool> tools = new HashSet<>();

    private McpMarshaller marshaller = new McpMarshaller();

    private McpServerConfiguration configuration;

    private int port = 8080;

    private WebSocketClient client;

    @Override
    public void run() {
        try {
            Quarkus.run(McpServerApplication.class, marshaller.json().writeValueAsString(configuration));
            logger.info("MCP server '{}' is running!", getName());
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to run MCP server", e);
        }

        client = Vertx.vertx().createWebSocketClient();
        client.connect(8080, "localhost", "/websocket")
            .onFailure(ex -> {
                logger.error("Failed to connect to mcp server websocket", ex);
            })
            .onSuccess(webSocket -> {
                webSocket.textMessageHandler(text -> {
                    Message toolResponse = getEndpointAdapter().handleMessage(new DefaultMessage(text));
                    webSocket.writeTextMessage(toolResponse.getPayload(String.class))
                            .onFailure(ex -> {
                                logger.error("Failed to send tool response to mcp server", ex);
                            });
                });
        });
    }

    @Override
    protected void startup() {
        configuration = new McpServerConfiguration(getName(), getDefaultTimeout(), tools);
    }

    @Override
    protected void shutdown() {
        client.close();
        Quarkus.blockingExit();
    }

    public void setTools(Set<McpTool> tools) {
        this.tools.addAll(tools);
    }

    public void addTool(McpTool tool) {
        this.tools.add(tool);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setMarshaller(McpMarshaller marshaller) {
        this.marshaller = marshaller;
    }

}
