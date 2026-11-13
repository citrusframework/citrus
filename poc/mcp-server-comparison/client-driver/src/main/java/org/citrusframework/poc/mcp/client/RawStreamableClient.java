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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal raw Streamable HTTP client with full JSON-RPC identity control.
 *
 * <p>The SDK client manages request IDs itself; isolation scenarios (equal request IDs
 * across sessions, reverse-order replies) need explicit IDs, so this client builds the
 * envelopes by hand over {@code java.net.http}. It parses both plain-JSON and SSE
 * POST responses and tracks {@code Mcp-Session-Id} headers. Test tooling only.
 */
public final class RawStreamableClient implements AutoCloseable {

    public record Response(String idJson, String resultJson, String errorJson) {
        public boolean isError() {
            return errorJson != null;
        }
    }

    private final HttpClient http;
    private final String baseUrl;

    public RawStreamableClient(String baseUrl, Duration timeout) {
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    /** Initializes a session; returns the server-issued session ID. */
    public String initialize() throws Exception {
        HttpRequest request = envelope(null, "\"init-1\"", "initialize",
                "{\"protocolVersion\": \"2025-11-25\", \"capabilities\": {}, "
                        + "\"clientInfo\": {\"name\": \"citrus-poc-raw\", \"version\": \"1.0.0\"}}");
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("initialize failed: " + response.statusCode() + " " + response.body());
        }
        List<String> sessions = response.headers().allValues("Mcp-Session-Id");
        if (sessions.isEmpty()) {
            // Some servers answer initialize without a session header on plain JSON.
            sessions = response.headers().allValues("mcp-session-id");
        }
        if (sessions.isEmpty()) {
            throw new IllegalStateException("no session id in initialize response");
        }
        return sessions.get(0);
    }

    /** Sends one JSON-RPC request on a session; matches the response by ID. */
    public Response post(String sessionId, String idJson, String method, String paramsJson) throws Exception {
        HttpRequest request = envelope(sessionId, idJson, method, paramsJson);
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 202) {
            throw new IllegalStateException(
                    method + " failed: " + response.statusCode() + " " + response.body());
        }
        return matchById(response.body(), idJson);
    }

    /** Best-effort session close (MCP DELETE); servers may already have gone away. */
    public void deleteSession(String sessionId) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl))
                    .DELETE()
                    .header("Mcp-Session-Id", sessionId)
                    .timeout(Duration.ofSeconds(5))
                    .build();
            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            // Session teardown is best-effort; shutdown assertions live elsewhere.
        }
    }

    @Override
    public void close() {
        // No owned resources beyond the shared HttpClient (no shutdown needed).
    }

    private HttpRequest envelope(String sessionId, String idJson, String method, String paramsJson) {
        String body = "{\"jsonrpc\": \"2.0\", \"id\": " + idJson + ", \"method\": \"" + method
                + "\", \"params\": " + paramsJson + "}";
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .timeout(Duration.ofSeconds(30));
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
        }
        return builder.build();
    }

    static Response matchById(String body, String idJson) {
        List<String> payloads = new ArrayList<>();
        for (String line : body.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("data:")) {
                payloads.add(trimmed.substring("data:".length()).trim());
            }
        }
        if (payloads.isEmpty() && trimmedIsJson(body)) {
            payloads.add(body.trim());
        }
        for (String payload : payloads) {
            if (payload.contains("\"id\":" + idJson) || payload.contains("\"id\": " + idJson)) {
                return new Response(idJson, field(payload, "result"), field(payload, "error"));
            }
        }
        throw new IllegalStateException("no response for id " + idJson + " in: " + body);
    }

    private static boolean trimmedIsJson(String body) {
        String trimmed = body.trim();
        return trimmed.startsWith("{");
    }

    /** Extracts a top-level JSON member substring (balanced), or null when absent. */
    static String field(String payload, String name) {
        String key = "\"" + name + "\"";
        int keyAt = payload.indexOf(key);
        if (keyAt < 0) {
            return null;
        }
        int colon = payload.indexOf(':', keyAt + key.length());
        int start = colon + 1;
        while (start < payload.length() && Character.isWhitespace(payload.charAt(start))) {
            start++;
        }
        if (start >= payload.length()) {
            return null;
        }
        char open = payload.charAt(start);
        if (open != '{' && open != '[') {
            int end = start;
            while (end < payload.length() && payload.charAt(end) != ',' && payload.charAt(end) != '}') {
                end++;
            }
            return payload.substring(start, end).trim();
        }
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < payload.length(); i++) {
            char c = payload.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{' || c == '[') {
                depth++;
            } else if (c == '}' || c == ']') {
                depth--;
                if (depth == 0) {
                    return payload.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    /** Exposes parsed members for report assertions without a JSON dependency. */
    public static Map<String, String> responseFieldsForReport(Response response) {
        return Map.of("id", String.valueOf(response.idJson()),
                "result", String.valueOf(response.resultJson()),
                "error", String.valueOf(response.errorJson()));
    }
}
