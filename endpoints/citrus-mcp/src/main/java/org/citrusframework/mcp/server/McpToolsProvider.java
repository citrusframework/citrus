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

import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolManager;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkus.runtime.Startup;
import io.quarkus.websockets.next.OpenConnections;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.mcp.model.McpMarshaller;
import org.citrusframework.mcp.model.McpServerConfiguration;
import org.citrusframework.mcp.model.McpTool;
import org.citrusframework.mcp.model.McpToolInvocation;
import org.citrusframework.mcp.model.McpToolResponse;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@ApplicationScoped
public class McpToolsProvider {

    private static final Logger log = LoggerFactory.getLogger(McpToolsProvider.class);
    private final ToolManager toolManager;

    private final McpServerConfiguration configuration;

    private final OpenConnections connections;

    private final BlockingQueue<McpToolResponse> responseQueue = new LinkedBlockingQueue<>();

    private final McpMarshaller marshaller = new McpMarshaller();

    public McpToolsProvider(ToolManager toolManager, OpenConnections connections, McpServerConfiguration configuration) {
        this.toolManager = toolManager;
        this.configuration = configuration;
        this.connections = connections;
    }

    @PostConstruct
    public void init() {
        for (McpTool tool : configuration.tools()) {
            ToolManager.ToolDefinition toolDefinition = toolManager.newTool(tool.name())
                    .setHandler(args -> handle(tool, args))
                    .setDescription(tool.description());

            tool.arguments().forEach(arg ->
                    toolDefinition.addArgument(arg.name(), arg.description(),
                            arg.required(), arg.type(), arg.defaultValue()));

            toolDefinition.register();
        }
    }

    public ToolResponse handle(McpTool tool, ToolManager.ToolArguments args) {
        McpToolInvocation invocation = McpToolInvocation.from(args.rawMessage());
        String json = marshaller.json().writer().writeValueAsString(invocation);

        connections.stream()
                .forEach(connection -> connection.sendTextAndAwait(json));

        try {
            McpToolResponse response = responseQueue.poll(configuration.timeout(), TimeUnit.MILLISECONDS);
            return getToolResponse(tool, response);
        } catch (InterruptedException e) {
            return new ToolResponse(true, Collections.singletonList(new TextContent("Failed to process MCP tool invocation")));
        }
    }

    private static ToolResponse getToolResponse(McpTool tool, McpToolResponse response) {
        if (response != null) {
            return new ToolResponse(response.error(), response.content().stream().map(TextContent::new).toList());
        } else if (tool.defaultResponse() != null) {
            return new ToolResponse(tool.defaultResponse().error(),
                    tool.defaultResponse().content().stream().map(TextContent::new).toList());
        } else {
            return new ToolResponse(true, Collections.singletonList(new TextContent("Failed to process MCP tool invocation")));
        }
    }

    @Incoming("tool-response")
    void onToolResponse(McpToolResponse response) {
        responseQueue.add(response);
    }
}
