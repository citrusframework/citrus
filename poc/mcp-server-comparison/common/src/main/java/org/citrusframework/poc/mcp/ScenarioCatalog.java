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
import java.util.Map;

/**
 * Small fixed catalog shared by both candidates (Stage 1 contract).
 *
 * <p>Tool {@code echo} with a JSON object input schema; text resources
 * {@code poc://alpha} and {@code poc://beta}. Static mode returns the declared
 * catalog. Controlled mode receives the entire unpaginated {@code resources/list}
 * operation and may return an empty list, either resource, both in the requested
 * test order, or a JSON-RPC error without changing the catalog.
 */
public final class ScenarioCatalog {

    public record Resource(String uri, String name, String mimeType, String text) {
    }

    public static final List<Resource> RESOURCES = List.of(
            new Resource("poc://alpha", "alpha", "text/plain", "alpha-content"),
            new Resource("poc://beta", "beta", "text/plain", "beta-content"));

    public static final String TOOL_NAME = "echo";
    public static final String TOOL_DESCRIPTION = "Echoes the typed input back for deterministic simulation";

    /** JSON object input schema exercising nested objects, arrays and scalars. */
    public static String toolInputSchema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "nested": { "type": "object", "properties": { "value": { "type": "string" } } },
                    "items": { "type": "array", "items": { "type": "string" } },
                    "count": { "type": "number" },
                    "flag": { "type": "boolean" },
                    "maybeNull": { "type": ["string", "null"] },
                    "optional": { "type": "string" }
                  },
                  "required": ["nested", "items", "count", "flag"]
                }
                """;
    }

    /** Sample typed arguments: nested object, array, number, boolean, explicit null, absent optional. */
    public static String sampleToolArguments(String expectedValue) {
        return """
                {
                  "nested": { "value": "%s" },
                  "items": ["a", "b"],
                  "count": 3,
                  "flag": true,
                  "maybeNull": null
                }
                """.formatted(expectedValue);
    }

    /** Controlled-list fixture payloads keyed by fixture name. */
    public static String resourceListFixture(String fixture) {
        return switch (fixture) {
            case "empty" -> """
                    { "resources": [] }
                    """;
            case "alpha" -> """
                    { "resources": [{ "uri": "poc://alpha", "name": "alpha", "mimeType": "text/plain" }] }
                    """;
            case "beta" -> """
                    { "resources": [{ "uri": "poc://beta", "name": "beta", "mimeType": "text/plain" }] }
                    """;
            case "alpha-beta" -> """
                    { "resources": [
                      { "uri": "poc://alpha", "name": "alpha", "mimeType": "text/plain" },
                      { "uri": "poc://beta", "name": "beta", "mimeType": "text/plain" }
                    ] }
                    """;
            case "beta-alpha" -> """
                    { "resources": [
                      { "uri": "poc://beta", "name": "beta", "mimeType": "text/plain" },
                      { "uri": "poc://alpha", "name": "alpha", "mimeType": "text/plain" }
                    ] }
                    """;
            default -> throw new IllegalArgumentException("unknown list fixture: " + fixture);
        };
    }

    /** Declared static catalog payload (both resources, declaration order). */
    public static String staticResourceList() {
        return resourceListFixture("alpha-beta");
    }

    public static Map<String, String> resourceContents() {
        return Map.of("poc://alpha", "alpha-content", "poc://beta", "beta-content");
    }

    private ScenarioCatalog() {
    }
}
