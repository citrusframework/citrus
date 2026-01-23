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

package org.citrusframework.functions.core;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatch;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.jsonp.JSONPModule;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Applies RFC 6902 JSON Patch operations to JSON content.
 * Supports JSONPath notation for paths, which is automatically converted to JSON Pointer format.
 *
 * @since 4.x
 */
public class JsonPatchFunction implements ParameterizedFunction<JsonPatchFunction.Parameters> {

    private static final JsonMapper jsonMapper = JsonMapper.builder()
            .addModule(new JSONPModule())
            .build();
    private static final List<String> VALID_OPS = List.of("add", "remove", "replace", "move", "copy");
    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("\\[(\\d+)]");

    @Override
    public String execute(Parameters params, TestContext context) {
        String jsonContent = context.replaceDynamicContentInString(params.getSource());

        JsonNode jsonNode;
        try {
            jsonNode = jsonMapper.readTree(jsonContent);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Source does not contain valid JSON: " + e.getMessage(), e);
        }

        List<String> patchOperations = new ArrayList<>();
        List<PatchOperation> operations = params.getOperations();

        for (PatchOperation op : operations) {
            patchOperations.add(convertToPatchOpJson(op));
        }

        String patchJson = "[" + String.join(",", patchOperations) + "]";

        try (JsonReader reader = Json.createReader(new StringReader(patchJson))) {
            JsonPatch patch = Json.createPatch(reader.readArray());
            JsonValue source = jsonMapper.convertValue(jsonNode, JsonValue.class);
            JsonObject patched = patch.apply(source.asJsonObject());
            return jsonMapper.writeValueAsString(patched);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to process JSON Patch", e);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    private String convertToPatchOpJson(PatchOperation op) {
        String jsonPointer = convertToJsonPointer(op.getPath());

        if ("remove".equals(op.getOperation())) {
            return String.format("{\"op\":\"%s\",\"path\":\"%s\"}", op.getOperation(), jsonPointer);
        } else if ("move".equals(op.getOperation()) || "copy".equals(op.getOperation())) {
            String fromPointer = convertToJsonPointer(op.getValue());
            return String.format("{\"op\":\"%s\",\"path\":\"%s\",\"from\":\"%s\"}",
                    op.getOperation(), jsonPointer, fromPointer);
        } else {
            String jsonValue;
            try {
                jsonMapper.readTree(op.getValue());
                jsonValue = op.getValue();
            } catch (Exception e) {
                jsonValue = "\"" + escapeJsonString(op.getValue()) + "\"";
            }
            return String.format("{\"op\":\"%s\",\"path\":\"%s\",\"value\":%s}",
                    op.getOperation(), jsonPointer, jsonValue);
        }
    }

    private String convertToJsonPointer(String jsonPath) {
        if (jsonPath == null || jsonPath.isEmpty()) {
            return "";
        }

        String pointer = jsonPath;

        if (pointer.startsWith("$.")) {
            pointer = pointer.substring(2);
        } else if (pointer.startsWith("$")) {
            pointer = pointer.substring(1);
        }

        Matcher matcher = ARRAY_INDEX_PATTERN.matcher(pointer);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "/" + matcher.group(1));
        }
        matcher.appendTail(sb);
        pointer = sb.toString();

        pointer = pointer.replace(".", "/");

        if (!pointer.startsWith("/")) {
            pointer = "/" + pointer;
        }

        return pointer;
    }

    private String escapeJsonString(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static class PatchOperation {

        private String operation;
        private String path;
        private String value;

        public PatchOperation() {
        }

        public PatchOperation(String operation, String path, String value) {
            this.operation = operation;
            this.path = path;
            this.value = value;
        }

        public String getOperation() {
            return operation;
        }

        @SchemaProperty(
                required = true,
                description = "JSON Patch operation type. One of: add, remove, replace, move, copy."
        )
        public void setOperation(String operation) {
            this.operation = operation;
        }

        public String getPath() {
            return path;
        }

        @SchemaProperty(
                required = true,
                description = "JSONPath expression to the target element (e.g. '$.items[0].name')."
        )
        public void setPath(String path) {
            this.path = path;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(
                description = "Value for add/replace operations, or source path for move/copy operations."
        )
        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Parameters implements FunctionParameters {
        private String source;
        private final List<PatchOperation> operations = new ArrayList<>();

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            if (parameterList.size() < 4) {
                throw new InvalidFunctionUsageException(
                        "Missing parameters - usage: jsonPatch('jsonSource', 'operation', 'path', 'value', ...)");
            }

            if ((parameterList.size() - 1) % 3 != 0) {
                throw new InvalidFunctionUsageException(
                        "Invalid parameter count - operations must be provided as triplets (operation, path, value)");
            }

            setSource(parameterList.get(0));

            for (int i = 1; i < parameterList.size(); i += 3) {
                String operation = context.replaceDynamicContentInString(parameterList.get(i));
                String path = context.replaceDynamicContentInString(parameterList.get(i + 1));
                String value = context.replaceDynamicContentInString(parameterList.get(i + 2));

                validateOperation(operation);
                operations.add(new PatchOperation(operation, path, value));
            }
        }

        private void validateOperation(String operation) {
            if (!VALID_OPS.contains(operation)) {
                throw new InvalidFunctionUsageException(
                        "Invalid patch operation '" + operation + "'. Valid operations are: " + String.join(", ", VALID_OPS));
            }
        }

        public String getSource() {
            return source;
        }

        @SchemaProperty(required = true, description = "The JSON source content to be patched.")
        public void setSource(String source) {
            this.source = source;
        }

        public List<PatchOperation> getOperations() {
            return operations;
        }

        @SchemaProperty(
                required = true,
                description = "List of JSON Patch operations (add, remove, replace, move, copy) applied to the source JSON."
        )
        public void setOperations(List<PatchOperation> operations) {
            this.operations.clear();
            if (operations != null) {
                this.operations.addAll(operations);
            }
        }
    }
}