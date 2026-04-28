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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.yaml.SchemaProperty;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Applies RFC 6902 JSON Patch operations to JSON content.
 * Supports JSONPath notation for paths, which is automatically converted to JSON Pointer format.
 *
 * @since 4.x
 */
public class JsonPatchFunction implements ParameterizedFunction<JsonPatchFunction.Parameters> {

    private static final JsonMapper jsonMapper = JsonMapper.shared();
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

        try {
            JsonNode result = jsonNode.deepCopy();
            for (PatchOperation op : params.getOperations()) {
                result = applyOperation(result, op);
            }
            return jsonMapper.writeValueAsString(result);
        } catch (Exception e) {
            throw new CitrusRuntimeException("Failed to apply JSON Patch: " + e.getMessage(), e);
        }
    }

    private JsonNode applyOperation(JsonNode root, PatchOperation op) {
        String jsonPointer = convertToJsonPointer(op.getPath());

        return switch (op.getOperation()) {
            case "add" -> add(root, jsonPointer, op.getValue());
            case "remove" -> remove(root, jsonPointer);
            case "replace" -> replace(root, jsonPointer, op.getValue());
            case "move" -> move(root, jsonPointer, convertToJsonPointer(op.getValue()));
            case "copy" -> copy(root, jsonPointer, convertToJsonPointer(op.getValue()));
            default -> throw new CitrusRuntimeException("Unsupported operation: " + op.getOperation());
        };
    }

    private JsonNode add(JsonNode root, String path, String value) {
        if (path.isEmpty() || path.equals("/")) {
            return parseValue(value);
        }

        String[] parts = path.substring(1).split("/");
        JsonNode current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            current = navigateToNode(current, parts[i]);
        }

        String lastPart = parts[parts.length - 1];
        JsonNode valueNode = parseValue(value);

        if (current.isArray()) {
            ArrayNode array = (ArrayNode) current;
            if ("-".equals(lastPart)) {
                array.add(valueNode);
            } else {
                int index = Integer.parseInt(lastPart);
                array.insert(index, valueNode);
            }
        } else if (current.isObject()) {
            ((ObjectNode) current).set(lastPart, valueNode);
        }

        return root;
    }

    private JsonNode remove(JsonNode root, String path) {
        if (path.isEmpty() || path.equals("/")) {
            throw new CitrusRuntimeException("Cannot remove root");
        }

        String[] parts = path.substring(1).split("/");
        JsonNode current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            current = navigateToNode(current, parts[i]);
        }

        String lastPart = parts[parts.length - 1];

        if (current.isArray()) {
            int index = Integer.parseInt(lastPart);
            if (index >= current.size()) {
                throw new CitrusRuntimeException("Array index out of bounds: " + index);
            }
            ((ArrayNode) current).remove(index);
        } else if (current.isObject()) {
            if (!current.has(lastPart)) {
                throw new CitrusRuntimeException("Path not found: " + path);
            }
            ((ObjectNode) current).remove(lastPart);
        }

        return root;
    }

    private JsonNode replace(JsonNode root, String path, String value) {
        remove(root, path);
        return add(root, path, value);
    }

    private JsonNode move(JsonNode root, String toPath, String fromPath) {
        JsonNode value = getNodeAtPath(root, fromPath);
        root = remove(root, fromPath);
        return add(root, toPath, value.toString());
    }

    private JsonNode copy(JsonNode root, String toPath, String fromPath) {
        JsonNode value = getNodeAtPath(root, fromPath);
        return add(root, toPath, value.toString());
    }

    private JsonNode getNodeAtPath(JsonNode root, String path) {
        if (path.isEmpty() || path.equals("/")) {
            return root;
        }

        String[] parts = path.substring(1).split("/");
        JsonNode current = root;

        for (String part : parts) {
            current = navigateToNode(current, part);
        }

        return current;
    }

    private JsonNode navigateToNode(JsonNode node, String part) {
        if (node == null) {
            throw new CitrusRuntimeException("Cannot navigate through null node");
        }

        JsonNode result;
        if (node.isArray()) {
            int index = Integer.parseInt(part);
            if (index >= node.size()) {
                throw new CitrusRuntimeException("Array index out of bounds: " + index);
            }
            result = node.get(index);
        } else if (node.isObject()) {
            result = node.get(part);
            if (result == null) {
                throw new CitrusRuntimeException("Path not found: " + part);
            }
        } else {
            throw new CitrusRuntimeException("Cannot navigate to: " + part);
        }

        return result;
    }

    private JsonNode parseValue(String value) {
        try {
            return jsonMapper.readTree(value);
        } catch (Exception e) {
            return jsonMapper.getNodeFactory().textNode(value);
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

        @SchemaProperty(required = true, description = "The JSON source content to be patched as inline data.")
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
