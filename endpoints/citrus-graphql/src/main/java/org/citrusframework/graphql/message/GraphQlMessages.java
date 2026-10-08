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
package org.citrusframework.graphql.message;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.message.HttpMessageHeaders;
import org.citrusframework.message.Message;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.support.DefaultGraphQlRequest;
import org.springframework.web.bind.annotation.RequestMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Converts between Citrus HTTP messages and Spring GraphQL's request and response model, following
 * GraphQL over HTTP: a POST request carries {@code {"query", "operationName", "variables", "extensions"}}
 * as JSON body, a GET request carries the same fields as form-encoded query parameters (with
 * {@code variables} and {@code extensions} JSON-encoded).
 */
public final class GraphQlMessages {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GraphQlMessages() {
        // utility class
    }

    /**
     * Reads the GraphQL request from a received HTTP message: from query parameters for GET, from the
     * JSON body otherwise.
     * @throws ValidationException when the message is not a valid GraphQL request
     */
    public static GraphQlRequest toRequest(Message message) {
        Map<String, Object> fields = isGet(message)
                ? fromQueryParams(String.valueOf(message.getHeaders().getOrDefault(HttpMessageHeaders.HTTP_QUERY_PARAMS, "")))
                : fromBody(message.getPayload(String.class));

        if (!fields.containsKey("query") || fields.get("query") == null) {
            throw new ValidationException("Invalid GraphQL request: missing 'query'");
        }

        return new DefaultGraphQlRequest(
                field(fields, "query", String.class, "a string"),
                field(fields, "operationName", String.class, "a string"),
                mapField(fields, "variables"),
                mapField(fields, "extensions"));
    }

    /**
     * Writes the JSON body of a POST request.
     */
    public static String toRequestBody(GraphQlRequest request) {
        return toJson(request.toMap());
    }

    /**
     * Writes the form-encoded query string of a GET request, without leading {@code ?}.
     */
    public static String toQueryString(GraphQlRequest request) {
        return request.toMap().entrySet().stream()
                .map(entry -> "%s=%s".formatted(entry.getKey(),
                        URLEncoder.encode(entry.getValue() instanceof String text ? text : toJson(entry.getValue()), UTF_8)))
                .collect(Collectors.joining("&"));
    }

    /**
     * Reads the GraphQL response from a message's JSON body.
     * @throws ValidationException when the body is not a JSON object
     */
    public static GraphQlResponse toResponse(Message message) {
        Map<String, Object> body = toJsonObject(message.getPayload(String.class));
        if (body == null) {
            throw new ValidationException("GraphQL response body is not a JSON object");
        }

        return new MapGraphQlResponse(body);
    }

    /**
     * Parses JSON text into a map; {@code null} when the text is empty, not valid JSON or not an object.
     */
    public static Map<String, Object> toJsonObject(String json) {
        return parseOrNull(json) instanceof Map<?, ?> map ? stringKeys(map) : null;
    }

    /**
     * Checks the response shape of GraphQL over HTTP: a JSON object with {@code data} and/or
     * {@code errors}, every error having a {@code message}.
     * @return the violation, {@code null} when the shape is valid
     */
    public static String shapeViolation(String payload) {
        Map<String, Object> body = toJsonObject(payload);
        if (body == null) {
            return NOT_A_JSON_OBJECT;
        }

        if (!body.containsKey("data") && !body.containsKey("errors")) {
            return "body has neither 'data' nor 'errors'";
        }

        if (body.get("errors") instanceof java.util.List<?> errors) {
            for (int i = 0; i < errors.size(); i++) {
                if (!(errors.get(i) instanceof Map<?, ?> error) || !(error.get("message") instanceof String)) {
                    return "error %d has no 'message'".formatted(i);
                }
            }
        }

        return null;
    }

    /** Shape violation of a body that is not a JSON object. */
    public static final String NOT_A_JSON_OBJECT = "body is not a JSON object";

    /**
     * Serializes a value as JSON.
     */
    public static String toJson(Object value) {
        return JSON.writeValueAsString(value);
    }

    /**
     * Parses JSON text; {@code null} when the text is empty or not valid JSON.
     */
    public static Object parseOrNull(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }

        try {
            return JSON.readValue(json, Object.class);
        } catch (JacksonException e) {
            return null;
        }
    }

    private static boolean isGet(Message message) {
        Object method = message.getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD);
        return method != null && RequestMethod.GET.name().equalsIgnoreCase(method.toString());
    }

    private static Map<String, Object> fromBody(String body) {
        if (!(parseOrNull(body) instanceof Map<?, ?> map)) {
            throw new ValidationException("Invalid GraphQL request: body is not a JSON object");
        }

        return stringKeys(map);
    }

    /**
     * The HTTP server stores the raw query string with {@code &} replaced by {@code ,} and literal
     * commas as {@code %2C}, so split on {@code ,} first and form-decode each part afterwards.
     */
    private static Map<String, Object> fromQueryParams(String queryParams) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (String token : queryParams.split(",")) {
            if (token.isEmpty()) {
                continue;
            }

            int separator = token.indexOf('=');
            String name = URLDecoder.decode(separator < 0 ? token : token.substring(0, separator), UTF_8);
            String value = separator < 0 ? "" : URLDecoder.decode(token.substring(separator + 1), UTF_8);
            fields.put(name, "variables".equals(name) || "extensions".equals(name) ? parseJsonField(name, value) : value);
        }

        return fields;
    }

    private static Object parseJsonField(String name, String value) {
        if (value.isBlank()) {
            return null;
        }

        Object parsed = parseOrNull(value);
        if (!(parsed instanceof Map)) {
            throw new ValidationException("Invalid GraphQL request: '%s' must be a JSON object".formatted(name));
        }

        return parsed;
    }

    private static <T> T field(Map<String, Object> fields, String name, Class<T> type, String description) {
        Object value = fields.get(name);
        if (value == null) {
            return null;
        }

        if (!type.isInstance(value)) {
            throw new ValidationException("Invalid GraphQL request: '%s' must be %s".formatted(name, description));
        }

        return type.cast(value);
    }

    private static Map<String, Object> mapField(Map<String, Object> fields, String name) {
        Object value = fields.get(name);
        if (value == null) {
            return Map.of();
        }

        if (!(value instanceof Map<?, ?> map)) {
            throw new ValidationException("Invalid GraphQL request: '%s' must be a JSON object".formatted(name));
        }

        return stringKeys(map);
    }

    private static Map<String, Object> stringKeys(Map<?, ?> map) {
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, value) -> result.put(String.valueOf(key), value));
        return result;
    }
}
