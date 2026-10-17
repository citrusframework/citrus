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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.mcp.server.McpServer;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.TextResourceContents;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Second named MCP server for the routing spike (T12-live).
 *
 * <p>Spike fixtures only (not the shared contract): tool {@code echoB} and text
 * resources {@code poc://gamma}, {@code poc://delta}. Wired to the same Citrus bridge
 * handoff with distinct selectors so any dispatch is attributable. Whether both
 * servers route independently in one augmented host is the spike question.
 */
@ApplicationScoped
@McpServer("poc-b")
public class CitrusMcpFeaturesB {

    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(description = "Spike echo on the named server poc-b")
    public TextContent echoB(@ToolArg(description = "Value") String value) {
        try {
            QuarkusBridgeHolder.awaitReady(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Citrus bridge handoff unavailable", e);
        }
        String requestId = "live-b-" + System.nanoTime();
        String payload;
        try {
            payload = mapper.writeValueAsString(Map.of("value", value));
        } catch (Exception e) {
            throw new IllegalArgumentException("cannot serialize tool arguments", e);
        }
        String token = QuarkusBridgeHolder.bridge().receive(QuarkusBridgeHolder.endpointId(),
                QuarkusBridgeHolder.generation(), "live-session", requestId,
                "tools/call", "echoB", payload);
        QuarkusBridgeHolder.bridge().reply(token,
                QuarkusBridgeHolder.supplier().replyFor("tools/call", "echoB", payload));
        try {
            org.citrusframework.poc.mcp.SimulationReply confirmed = QuarkusBridgeHolder.bridge()
                    .registry().await(token, QuarkusBridgeHolder.simulationTimeoutSeconds(), TimeUnit.SECONDS);
            QuarkusBridgeHolder.markServed("tools/call", "echoB");
            return new TextContent(confirmed.getJsonPayload());
        } catch (Exception e) {
            throw new IllegalStateException("simulation reply unavailable for " + token, e);
        }
    }

    @Resource(uri = "poc://gamma", name = "gamma", title = "gamma",
            description = "Spike resource gamma on poc-b", mimeType = "text/plain")
    public TextResourceContents gamma() {
        return new TextResourceContents("poc://gamma", "gamma-content", "text/plain");
    }

    @Resource(uri = "poc://delta", name = "delta", title = "delta",
            description = "Spike resource delta on poc-b", mimeType = "text/plain")
    public TextResourceContents delta() {
        return new TextResourceContents("poc://delta", "delta-content", "text/plain");
    }
}
