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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.citrusframework.poc.mcp.CitrusPocEndpoint;
import org.citrusframework.poc.mcp.ScenarioCatalog;
import org.citrusframework.poc.mcp.SimulationReply;

/**
 * Normal Spring tool/resource wiring for HTTP round trips (T9).
 *
 * <p>Same dispatch contract as the Quarkus path, using Spring AI tool/resource
 * specifications with supported asynchronous behavior in the live experiment.
 * Validates the actual Citrus runner rather than hardcoded callback results;
 * keeps the declared SDK version and high-level server intact.
 */
public final class SpringFeatures {

    private final CitrusPocEndpoint bridge;

    public SpringFeatures(CitrusPocEndpoint bridge) {
        this.bridge = bridge;
    }

    public Map<String, String> toolDescriptor() {
        return Map.of(
                "name", ScenarioCatalog.TOOL_NAME,
                "description", ScenarioCatalog.TOOL_DESCRIPTION,
                "inputSchema", ScenarioCatalog.toolInputSchema());
    }

    public Map<String, String> resourceDescriptor(String uri) {
        return ScenarioCatalog.resourceContents().containsKey(uri)
                ? Map.of("uri", uri, "mimeType", "text/plain",
                        "text", ScenarioCatalog.resourceContents().get(uri))
                : Map.of("uri", uri, "unknown", "true");
    }

    public SimulationReply dispatchToolCall(String endpointId, int generation, String sessionId,
            String requestId, String toolName, String argumentsJson, long timeoutSeconds) throws Exception {
        String token = bridge.receive(endpointId, generation, sessionId, requestId,
                "tools/call", toolName, argumentsJson);
        return bridge.registry().await(token, timeoutSeconds, TimeUnit.SECONDS);
    }

    public SimulationReply dispatchResourceRead(String endpointId, int generation, String sessionId,
            String requestId, String uri, long timeoutSeconds) throws Exception {
        String token = bridge.receive(endpointId, generation, sessionId, requestId,
                "resources/read", uri, "{\"uri\": \"" + uri + "\"}");
        return bridge.registry().await(token, timeoutSeconds, TimeUnit.SECONDS);
    }
}
