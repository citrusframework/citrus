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

package org.citrusframework.poc.mcp.spring;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.definition.ToolDefinition;

/**
 * Citrus-controlled {@code echo} tool for the live Spring candidate.
 *
 * <p>Registered as a {@link ToolCallback} bean — the documented public extension consumed by
 * {@code ToolCallbackConverterAutoConfiguration} into the Spring AI-managed high-level
 * {@code McpSyncServer}. Protocol ownership (init, capabilities, dispatch, sessions, error
 * serialization) stays with Spring AI and its SDK 2.0.0; Citrus owns only scenario
 * expectations and replies. Wire request IDs are framework-managed; bridge correlation
 * uses endpoint + generation + framework session + per-call nonce (documented in the report).
 */
public final class CitrusToolCallback implements ToolCallback {

    private final ToolDefinition definition = DefaultToolDefinition.builder()
            .name(ScenarioCatalog.TOOL_NAME)
            .description(ScenarioCatalog.TOOL_DESCRIPTION)
            .inputSchema(ScenarioCatalog.toolInputSchema())
            .build();

    private final CitrusPocEndpoint bridge;
    private final ScenarioReplySupplier supplier;
    private final String endpointId;
    private final int generation;
    private final long simulationTimeoutSeconds;
    private final AtomicLong calls = new AtomicLong();
    private final AtomicLong settled = new AtomicLong();
    private volatile Throwable lastFailure;

    public CitrusToolCallback(CitrusPocEndpoint bridge, ScenarioReplySupplier supplier,
            String endpointId, int generation, long simulationTimeoutSeconds) {
        this.bridge = bridge;
        this.supplier = supplier;
        this.endpointId = endpointId;
        this.generation = generation;
        this.simulationTimeoutSeconds = simulationTimeoutSeconds;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return definition;
    }

    @Override
    public String call(String toolInput) {
        // Real Citrus receive: validates the typed dispatch, registers the exchange.
        // A deliberate expectation mismatch fails here, before any reply exists.
        String requestId = "live-" + calls.incrementAndGet();
        try {
            String token = bridge.receive(endpointId, generation, "live-session", requestId,
                    "tools/call", ScenarioCatalog.TOOL_NAME, toolInput);
            SimulationReply reply = supplier.replyFor("tools/call", ScenarioCatalog.TOOL_NAME, toolInput);
            bridge.reply(token, reply);
            try {
                SimulationReply confirmed = bridge.registry().await(token, simulationTimeoutSeconds, TimeUnit.SECONDS);
                return confirmed.getJsonPayload();
            } catch (Exception e) {
                throw new IllegalStateException("simulation reply unavailable for " + token, e);
            }
        } catch (RuntimeException | Error e) {
            lastFailure = e;
            throw e;
        } finally {
            settled.incrementAndGet();
        }
    }

    /** Calls settled (replied or failed); the scenario main polls this with a deadline. */
    public long callsSettled() {
        return settled.get();
    }

    /** Last Citrus-side failure observed in a tool call, if any. */
    public Throwable lastFailure() {
        return lastFailure;
    }

}
