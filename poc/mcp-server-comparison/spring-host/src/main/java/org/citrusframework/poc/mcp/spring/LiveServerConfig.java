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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Live Spring AI MCP server configuration for one owned context generation.
 *
 * <p>Declares the Citrus-controlled {@code echo} tool (via the documented
 * {@link ToolCallback} extension) and the two static text resources (via
 * {@link McpServerFeatures.SyncResourceSpecification}, likewise consumed by
 * {@code McpServerAutoConfiguration} into the high-level {@code McpSyncServer}).
 * Static resource registration is the control case; the whole-list customizer from
 * T7 stays a separate investigation. A low-level session-factory replacement is a
 * boundary-changing alternative for review, not this PoC.
 */
@Configuration
@EnableAutoConfiguration
public class LiveServerConfig {

    private final AtomicLong resourceCalls = new AtomicLong();

    @Bean
    public ToolCallback citrusEchoTool(CitrusPocEndpoint bridge, ScenarioReplySupplier supplier,
            LiveIdentity identity) {
        return new CitrusToolCallback(bridge, supplier, identity.endpointId(),
                identity.generation(), identity.simulationTimeoutSeconds());
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> citrusStaticResources(
            CitrusPocEndpoint bridge, ScenarioReplySupplier supplier, LiveIdentity identity) {
        return ScenarioCatalog.RESOURCES.stream()
                .map(resource -> new McpServerFeatures.SyncResourceSpecification(
                        new McpSchema.Resource(resource.uri(), resource.name(), resource.name(),
                                resource.name(), "text/plain", (long) resource.text().length(), null, null),
                        (exchange, request) -> {
                            String requestId = "live-res-" + resourceCalls.incrementAndGet();
                            String token = bridge.receive(identity.endpointId(), identity.generation(),
                                    exchange.sessionId(), requestId,
                                    "resources/read", request.uri(), "{\"uri\": \"" + request.uri() + "\"}");
                            SimulationReply reply =
                                    supplier.replyFor("resources/read", request.uri(), request.uri());
                            bridge.reply(token, reply);
                            try {
                                SimulationReply confirmed = bridge.registry()
                                        .await(token, identity.simulationTimeoutSeconds(), TimeUnit.SECONDS);
                                return new McpSchema.ReadResourceResult(List.of(
                                        new McpSchema.TextResourceContents(request.uri(), "text/plain",
                                                confirmed.getJsonPayload())));
                            } catch (Exception e) {
                                throw new IllegalStateException("simulation reply unavailable for " + token, e);
                            }
                        }))
                .toList();
    }

    /** Per-generation identity carried as a singleton (registered before refresh). */
    public record LiveIdentity(String endpointId, int generation, long simulationTimeoutSeconds) {
    }
}
