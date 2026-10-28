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

package org.citrusframework.poc.mcp.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * Test-only official SDK client (pinned 2.0.1) driving both candidates over stdio
 * and Streamable HTTP. Lives on this driver's isolated classpath only — never on a
 * host runtime classpath. Server-side Citrus assertions are recorded separately in
 * the probe/child report; this client records only what the client observed.
 */
public final class LiveMcpClient implements AutoCloseable {

    private McpSyncClient client;
    private final io.modelcontextprotocol.spec.McpClientTransport transport;
    private final StdioClientTransport stdioTransport;
    private final List<String> childStderr = new CopyOnWriteArrayList<>();
    private final ExecutorService exits = Executors.newSingleThreadExecutor();

    private LiveMcpClient(io.modelcontextprotocol.spec.McpClientTransport transport,
            StdioClientTransport stdioTransport) {
        this.transport = transport;
        this.stdioTransport = stdioTransport;
    }

    /**
     * Launches the server child with an explicit argument array (never a shell string)
     * and connects the SDK stdio transport. Child diagnostics go to {@link #childStderr}.
     */
    public static LiveMcpClient launchStdio(List<String> command, Duration requestTimeout) {
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("child command must not be empty");
        }
        if (command.stream().anyMatch(arg -> arg.contains("&&") || arg.contains("|") || arg.contains(";"))) {
            throw new IllegalArgumentException("command must be an argument array, not a shell string");
        }
        ServerParameters params = ServerParameters.builder(command.get(0))
                .args(command.subList(1, command.size()))
                .build();
        StdioClientTransport transport =
                new StdioClientTransport(params, io.modelcontextprotocol.json.McpJsonDefaults.getMapper());
        LiveMcpClient launched = new LiveMcpClient(transport, transport);
        // Single shared sink set before connect so no early diagnostic line is lost.
        transport.setStdErrorHandler(launched.childStderr::add);
        launched.client = McpClient.sync(transport)
                .requestTimeout(requestTimeout)
                .clientInfo(new McpSchema.Implementation("citrus-poc-driver", "1.0.0"))
                .build();
        return launched;
    }

    public McpSchema.InitializeResult initialize() {
        return client.initialize();
    }

    public McpSchema.ListToolsResult listTools() {
        return client.listTools();
    }

    public McpSchema.CallToolResult callTool(String name, Map<String, Object> arguments) {
        return client.callTool(new McpSchema.CallToolRequest(name, arguments));
    }

    public McpSchema.ListResourcesResult listResources() {
        return client.listResources();
    }

    public McpSchema.ReadResourceResult readResource(String uri) {
        return client.readResource(new McpSchema.ReadResourceRequest(uri));
    }

    /**
     * Connects to a Streamable HTTP endpoint (server already running, e.g. an HTTP
     * scenario child past its readiness marker). Child and server lifecycles stay
     * with the caller; this client only observes protocol traffic.
     */
    public static LiveMcpClient connectHttp(String baseUrl, Duration requestTimeout) {
        io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport transport =
                io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport.builder(baseUrl)
                        .build();
        LiveMcpClient launched = new LiveMcpClient(transport, null);
        launched.client = McpClient.sync(transport)
                .requestTimeout(requestTimeout)
                .clientInfo(new McpSchema.Implementation("citrus-poc-driver", "1.0.0"))
                .build();
        return launched;
    }

    public List<String> childStderr() {
        return List.copyOf(childStderr);
    }

    /**
     * Awaits natural stdio child exit within the bound. A timeout or forced kill is
     * shutdown failure, never success — callers must record it as such. HTTP clients
     * have no owned child; use {@link ManagedChildProcess} instead.
     */
    public int awaitChildExit(long timeout, TimeUnit unit) throws Exception {
        if (stdioTransport == null) {
            throw new IllegalStateException("no owned child (HTTP client)");
        }
        Future<?> waiting = exits.submit(() -> {
            stdioTransport.awaitForExit();
            return null;
        });
        try {
            waiting.get(timeout, unit);
        } catch (java.util.concurrent.TimeoutException e) {
            waiting.cancel(true);
            throw e;
        }
        return 0;
    }

    @Override
    public void close() {
        try {
            client.closeGracefully();
        } finally {
            exits.shutdownNow();
        }
    }

    /** Collects stderr lines matching a predicate (e.g. child Citrus assertion summaries). */
    public void drainStderrTo(Consumer<String> line) {
        childStderr.forEach(line);
    }
}
