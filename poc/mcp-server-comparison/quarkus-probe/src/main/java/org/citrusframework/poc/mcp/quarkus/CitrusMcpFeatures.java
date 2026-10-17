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

package org.citrusframework.poc.mcp.quarkus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Citrus-controlled MCP features for the live Quarkus candidate.
 *
 * <p>Annotated CDI bean methods — the normal Quarkus MCP extension programming model
 * consumed by the augmented runtime into its protocol server. Framework-owned behavior
 * (init, capabilities, dispatch, sessions, framing, error serialization) stays with
 * Quarkus MCP 2.0.2; Citrus owns only scenario expectations and replies through the
 * bridge. Wire request IDs stay framework-managed; bridge correlation uses
 * endpoint + generation + per-call nonce (documented in the report).
 */
@ApplicationScoped
public class CitrusMcpFeatures {

    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicLong calls = new AtomicLong();
    private volatile Throwable lastFailure;

    @Tool(description = "Echoes the typed input back for deterministic simulation")
    public TextContent echo(
            @ToolArg(description = "Nested object with a value") Map<String, Object> nested,
            @ToolArg(description = "Items") List<String> items,
            @ToolArg(description = "Count") int count,
            @ToolArg(description = "Flag") boolean flag,
            @ToolArg(required = false, description = "Nullable text (elided on the wire per LIVE-NULL-1)") String maybeNull) {
        awaitBridge();
        Map<String, Object> delivered = new LinkedHashMap<>();
        delivered.put("nested", nested);
        delivered.put("items", items);
        delivered.put("count", count);
        delivered.put("flag", flag);
        if (maybeNull != null) {
            delivered.put("maybeNull", maybeNull);
        }
        String payload = writeJson(delivered);
        String requestId = "live-" + calls.incrementAndGet();
        try {
            String token = QuarkusBridgeHolder.bridge().receive(QuarkusBridgeHolder.endpointId(),
                    QuarkusBridgeHolder.generation(), "live-session", requestId,
                    "tools/call", "echo", payload);
            SimulationReply reply = QuarkusBridgeHolder.supplier().replyFor("tools/call", "echo", payload);
            QuarkusBridgeHolder.bridge().reply(token, reply);
            try {
                SimulationReply confirmed = QuarkusBridgeHolder.bridge().registry()
                        .await(token, QuarkusBridgeHolder.simulationTimeoutSeconds(), TimeUnit.SECONDS);
                QuarkusBridgeHolder.markServed("tools/call", "echo");
                return new TextContent(confirmed.getJsonPayload());
            } catch (Exception e) {
                throw new IllegalStateException("simulation reply unavailable for " + token, e);
            }
        } catch (RuntimeException | Error e) {
            lastFailure = e;
            throw e;
        }
    }

    @Resource(uri = "poc://alpha", name = "alpha", title = "alpha",
            description = "PoC text resource alpha", mimeType = "text/plain")
    public TextResourceContents alpha() {
        return read("poc://alpha", "alpha-content");
    }

    @Resource(uri = "poc://beta", name = "beta", title = "beta",
            description = "PoC text resource beta", mimeType = "text/plain")
    public TextResourceContents beta() {
        return read("poc://beta", "beta-content");
    }

    private TextResourceContents read(String uri, String fallback) {
        awaitBridge();
        String requestId = "live-res-" + calls.incrementAndGet();
        try {
            String token = QuarkusBridgeHolder.bridge().receive(QuarkusBridgeHolder.endpointId(),
                    QuarkusBridgeHolder.generation(), "live-session", requestId,
                    "resources/read", uri, "{\"uri\": \"" + uri + "\"}");
            SimulationReply reply = QuarkusBridgeHolder.supplier().replyFor("resources/read", uri, uri);
            QuarkusBridgeHolder.bridge().reply(token, reply);
            try {
                SimulationReply confirmed = QuarkusBridgeHolder.bridge().registry()
                        .await(token, QuarkusBridgeHolder.simulationTimeoutSeconds(), TimeUnit.SECONDS);
                QuarkusBridgeHolder.markServed("resources/read", uri);
                return new TextResourceContents(uri, confirmed.getJsonPayload(), "text/plain");
            } catch (Exception e) {
                throw new IllegalStateException("simulation reply unavailable for " + token, e);
            }
        } catch (RuntimeException | Error e) {
            lastFailure = e;
            throw e;
        }
    }

    public Throwable lastFailure() {
        return lastFailure;
    }

    private static void awaitBridge() {
        try {
            QuarkusBridgeHolder.awaitReady(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Citrus bridge handoff unavailable", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot serialize tool arguments", e);
        }
    }
}
