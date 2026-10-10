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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;

/**
 * Resolves the variables of a GraphQL request. Citrus expressions are resolved in every string value;
 * values keep their JSON type: a number stays a number, a boolean a boolean and a string a string —
 * also when the string came from a Citrus variable. To send a typed value taken from a Citrus
 * variable, use the variables JSON object, e.g. {@code {"first": ${n}}}: it is resolved first and
 * parsed afterwards.
 */
public final class GraphQlVariables {

    private GraphQlVariables() {
        // utility class
    }

    /**
     * Merges the variables JSON object and the single variables; single variables win on name clashes.
     * @param variables single variables by name, may be {@code null}
     * @param variablesJson JSON object with variables, may be {@code null} or blank
     * @throws ValidationException when the variables JSON is invalid or not an object
     */
    public static Map<String, Object> resolve(Map<String, ?> variables, String variablesJson, TestContext context) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        if (variablesJson != null && !variablesJson.isBlank()) {
            String json = context.replaceDynamicContentInString(variablesJson);
            Object parsed = GraphQlMessages.parseOrNull(json);
            if (parsed == null) {
                throw new ValidationException("Invalid GraphQL variables: %s".formatted(json));
            }

            if (!(parsed instanceof Map<?, ?> map)) {
                throw new ValidationException("Invalid GraphQL variables: must be a JSON object");
            }

            map.forEach((name, value) -> resolved.put(String.valueOf(name), value));
        }

        if (variables != null) {
            variables.forEach((name, value) -> resolved.put(name, resolveValue(value, context)));
        }

        return resolved;
    }

    private static Object resolveValue(Object value, TestContext context) {
        if (value instanceof String text) {
            return context.replaceDynamicContentInString(text);
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> resolved = new LinkedHashMap<>();
            map.forEach((name, nested) -> resolved.put(String.valueOf(name), resolveValue(nested, context)));
            return resolved;
        }

        if (value instanceof List<?> list) {
            return list.stream().map(item -> resolveValue(item, context)).toList();
        }

        return value;
    }
}
