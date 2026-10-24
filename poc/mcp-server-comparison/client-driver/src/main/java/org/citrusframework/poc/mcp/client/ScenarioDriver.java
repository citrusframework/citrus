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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs one named scenario and writes structured evidence. The driver keeps client
 * results separate from server-side Citrus assertions: this file records what the
 * client observed; the probe report records the Citrus scenario outcome.
 *
 * <p>The MCP request executor is injected so unit tests exercise correlation
 * (concurrent sessions, equal request IDs, reverse replies) without a live server.
 * Live runs wire the pinned SDK 2.0.1 client on this driver's isolated classpath.
 */
public final class ScenarioDriver {

    /** One JSON-RPC-shaped request/response exchange observed by the client. */
    public record Exchange(String sessionId, String requestId, String method,
            String requestJson, String responseJson, boolean toolError) {
    }

    /** Transport for one exchange; live implementation uses the SDK client. */
    public interface RequestExecutor {
        Exchange execute(String sessionId, String requestId, String method, String requestJson) throws Exception;
    }

    private final RequestExecutor executor;

    public ScenarioDriver(RequestExecutor executor) {
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    /** Runs initialize + list tools/resources + echo call + resource read sequentially. */
    public List<Exchange> runRoundTrip(String sessionId) throws Exception {
        List<Exchange> exchanges = new ArrayList<>();
        exchanges.add(executor.execute(sessionId, "req-init", "initialize", "{\"protocolVersion\": \"2025-11-25\"}"));
        exchanges.add(executor.execute(sessionId, "req-tools", "tools/list", "{}"));
        exchanges.add(executor.execute(sessionId, "req-resources", "resources/list", "{}"));
        exchanges.add(executor.execute(sessionId, "req-call", "tools/call",
                "{\"name\": \"echo\", \"arguments\": {\"nested\": {\"value\": \"probe\"}}}"));
        exchanges.add(executor.execute(sessionId, "req-read", "resources/read", "{\"uri\": \"poc://alpha\"}"));
        return exchanges;
    }

    /**
     * Runs two sessions with equal request IDs concurrently; returns both sessions'
     * exchanges. Callers reply in reverse order on the server side; this method only
     * proves the client can issue the concurrent pair.
     */
    public List<Exchange> runConcurrentEqualIds(String sessionA, String sessionB) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Exchange> taskA = () -> executor.execute(sessionA, "req-1", "resources/list", "{}");
            Callable<Exchange> taskB = () -> executor.execute(sessionB, "req-1", "resources/list", "{}");
            Future<Exchange> futureA = pool.submit(taskA);
            Future<Exchange> futureB = pool.submit(taskB);
            return List.of(futureA.get(), futureB.get());
        } finally {
            pool.shutdown();
        }
    }

    /** Writes minimal structured evidence (JSON) to the descriptor's result file. */
    public Path writeEvidence(LaunchDescriptor descriptor, List<Exchange> exchanges, String serverAssertions)
            throws Exception {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"candidate\": \"").append(descriptor.getCandidate()).append("\",\n");
        json.append("  \"transport\": \"").append(descriptor.getTransport()).append("\",\n");
        json.append("  \"scenario\": \"").append(descriptor.getScenarioId()).append("\",\n");
        json.append("  \"at\": \"").append(Instant.now()).append("\",\n");
        json.append("  \"clientSdk\": \"2.0.1\",\n");
        json.append("  \"exchanges\": ").append(exchanges.size()).append(",\n");
        json.append("  \"serverAssertions\": \"").append(escape(serverAssertions)).append("\"\n");
        json.append("}\n");
        Path path = Path.of(descriptor.getResultFile());
        Files.createDirectories(path.toAbsolutePath().getParent());
        Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
        return path;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
