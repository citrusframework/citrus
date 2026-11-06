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

package org.citrusframework.poc.mcp;

import java.util.List;
import java.util.Objects;

/**
 * Typed simulation reply supplied by the Citrus scenario. The framework adapter maps
 * this to its own protocol types and serialization.
 *
 * <p>A tool error remains a tool result (not a JSON-RPC error). Malformed test output
 * fails locally and terminates the pending exchange explicitly.
 */
public final class SimulationReply {

    public enum Kind {
        TOOL_RESULT,
        RESOURCE_READ_RESULT,
        RESOURCE_LIST_RESULT,
        JSON_RPC_ERROR
    }

    private final Kind kind;
    private final String jsonPayload;
    private final boolean toolError;
    private final Integer jsonRpcCode;

    private SimulationReply(Kind kind, String jsonPayload, boolean toolError, Integer jsonRpcCode) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.jsonPayload = Objects.requireNonNull(jsonPayload, "jsonPayload");
        this.toolError = toolError;
        this.jsonRpcCode = jsonRpcCode;
    }

    public static SimulationReply toolResult(String jsonPayload) {
        return new SimulationReply(Kind.TOOL_RESULT, jsonPayload, false, null);
    }

    public static SimulationReply toolErrorResult(String jsonPayload) {
        return new SimulationReply(Kind.TOOL_RESULT, jsonPayload, true, null);
    }

    public static SimulationReply resourceReadResult(String jsonPayload) {
        return new SimulationReply(Kind.RESOURCE_READ_RESULT, jsonPayload, false, null);
    }

    public static SimulationReply resourceListResult(String jsonPayload) {
        return new SimulationReply(Kind.RESOURCE_LIST_RESULT, jsonPayload, false, null);
    }

    public static SimulationReply jsonRpcError(int code, String jsonPayload) {
        return new SimulationReply(Kind.JSON_RPC_ERROR, jsonPayload, false, code);
    }

    public Kind getKind() {
        return kind;
    }

    public String getJsonPayload() {
        return jsonPayload;
    }

    /** True when this is an intentional tool-error result (still a tool result on the wire). */
    public boolean isToolError() {
        return toolError;
    }

    public Integer getJsonRpcCode() {
        return jsonRpcCode;
    }

    public static List<String> allowedResourceListFixtures() {
        return List.of("empty", "alpha", "beta", "alpha-beta", "beta-alpha");
    }
}
