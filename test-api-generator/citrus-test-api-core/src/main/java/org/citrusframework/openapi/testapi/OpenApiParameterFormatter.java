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

package org.citrusframework.openapi.testapi;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.citrusframework.exceptions.CitrusRuntimeException;

import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static org.citrusframework.openapi.testapi.ParameterStyle.DEEPOBJECT;
import static org.citrusframework.openapi.testapi.ParameterStyle.FORM;
import static org.citrusframework.openapi.testapi.ParameterStyle.LABEL;
import static org.citrusframework.openapi.testapi.ParameterStyle.MATRIX;
import static org.citrusframework.openapi.testapi.ParameterStyle.NONE;
import static org.citrusframework.openapi.testapi.ParameterStyle.PIPEDELIMITED;
import static org.citrusframework.openapi.testapi.ParameterStyle.SIMPLE;
import static org.citrusframework.openapi.testapi.ParameterStyle.SPACEDELIMITED;
import static org.citrusframework.openapi.testapi.ParameterStyle.X_ENCODE_AS_JSON;

class OpenApiParameterFormatter {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static final String PARAMETER_NAME_TOKEN = "%PARAMETER_NAME_TOKEN%";

    private static final Map<ParameterStyle, StyleEncoder> ENCODER_LOOKUP = Map.of(
        NONE, new NoneEncoder(),
        MATRIX, new MatrixEncoder(),
        LABEL, new LabelEncoder(),
        SIMPLE, new SimpleEncoder(new FormatParameters(PARAMETER_NAME_TOKEN + "=", ",")),
        FORM, new FormEncoder(),
        SPACEDELIMITED, new SimpleEncoder(new FormatParameters(PARAMETER_NAME_TOKEN + "=", "%20")),
        PIPEDELIMITED, new SimpleEncoder(new FormatParameters(PARAMETER_NAME_TOKEN + "=", "%7C")),
        DEEPOBJECT, new DeepObjectEncoder(),
        X_ENCODE_AS_JSON, new XEncodeAsJsonEncoder()
    );

    private OpenApiParameterFormatter() {
        // Static access only.
    }

    /**
     * Formats a list of values as a single String based on the separator and other settings.
     */
    static String formatAccordingToStyle(String parameterName,
        Object parameterValue,
        ParameterStyle parameterStyle,
        boolean explode,
        boolean isObject) {

        StyleEncoder styleEncoder = ENCODER_LOOKUP.get(parameterStyle);
        if (styleEncoder != null) {
            return styleEncoder.encodeAccordingToSpec(parameterName, parameterValue, explode,
                isObject);
        }

        throw new IllegalArgumentException(
            String.format("Parameter style '%s' is not supported", parameterStyle));
    }

    private static String formatExploded(List<String> values, String delimiter) {
        return IntStream.range(0, values.size() / 2)
            .mapToObj(i -> values.get(2 * i) + "=" + values.get(2 * i + 1))
            .collect(joining(delimiter));
    }

    private static List<String> toList(Object value, boolean isObject) {
        if (value == null) {
            return emptyList();
        }

        if (value.getClass().isArray()) {
            List<String> list = new ArrayList<>();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object singleValue = Array.get(value, i);
                list.add(singleValue.toString());
            }
            return list;
        } else if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        } else if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                .flatMap(entry -> Stream.of(entry.getKey().toString(), entry.getValue().toString()))
                .toList();
        } else if (isObject && value instanceof String jsonString) {
            return toList(convertJsonToMap(jsonString), true);
        } else if (isObject) {
            return toList(convertBeanToMap(value), true);
        } else {
            return singletonList(value.toString());
        }
    }

    private static Map<String, Object> convertJsonToMap(String jsonString) {
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to convert jsonString to Json object.", e);
        }

        if (!rootNode.isObject()) {
            throw new IllegalArgumentException("The provided string is not a valid Json object.");
        }

        return convertNodeToMap(rootNode);
    }

    private static Map<String, Object> convertNodeToMap(JsonNode objectNode) {
        Map<String, Object> resultMap = new TreeMap<>();

        Iterator<Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            JsonNode valueNode = field.getValue();

            if (valueNode.isObject() || valueNode.isArray()) {
                resultMap.put(field.getKey(), convertNodeToMap(valueNode));
            } else {
                resultMap.put(field.getKey(), valueNode.asText());
            }
        }

        return resultMap;
    }

    protected static Map<String, Object> convertBeanToMap(Object bean) {
        Map<String, Object> map = new TreeMap<>();
        try {
            Arrays.stream(Introspector.getBeanInfo(
                bean.getClass(), Object.class).getPropertyDescriptors()).sorted(
                comparing(FeatureDescriptor::getName)).forEach(propertyDescriptor -> {
                try {
                    String propertyName = propertyDescriptor.getName();
                    Object propertyValue = propertyDescriptor.getReadMethod().invoke(bean);
                    if (propertyValue != null) {
                        map.put(propertyName, propertyValue);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new CitrusRuntimeException(
                        "Error converting bean to map: " + e.getMessage(), e);
                }
            });
        } catch (IntrospectionException e) {
            throw new CitrusRuntimeException("Error converting bean to map: " + e.getMessage(), e);
        }
        return map;
    }

    private record FormatParameters(String prefix, String separator) {

    }

    private interface StyleEncoder {

        String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject);
    }

    private static class NoneEncoder implements StyleEncoder {

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            return parameterName + "=" + (parameterValue != null ? parameterValue.toString()
                : null);
        }
    }

    private static class MatrixEncoder implements StyleEncoder {

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            List<String> values = toList(parameterValue, isObject);
            FormatParameters formatParameters = matrixFormatParameters(parameterName, explode,
                isObject);

            String prefix = formatParameters.prefix.replace(PARAMETER_NAME_TOKEN, parameterName);

            String encoded;
            if (isObject && explode) {
                encoded = prefix + formatExploded(values, formatParameters.separator);
            } else {
                encoded = prefix + values.stream()
                    .collect(joining(formatParameters.separator));
            }

            return encoded;
        }

        private static FormatParameters matrixFormatParameters(String parameterName,
            boolean explode,
            boolean isObject) {
            String prefix;
            String separator = ",";
            if (explode) {
                if (isObject) {
                    prefix = ";";
                } else {
                    prefix = ";" + parameterName + "=";
                }
                separator = prefix;
            } else {
                prefix = ";" + parameterName + "=";
            }

            return new FormatParameters(PARAMETER_NAME_TOKEN + "=" + prefix, separator);
        }
    }

    private static class LabelEncoder implements StyleEncoder {

        private static final FormatParameters DEFAULT_LABEL_FORMAT_PARAMETERS = new FormatParameters(
            "%PARAMETER_NAME_TOKEN%=.", ",");
        private static final FormatParameters DEFAULT_LABEL_EXPLODED_PARAMETERS = new FormatParameters(
            "%PARAMETER_NAME_TOKEN%=.", ".");

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            List<String> values = toList(parameterValue, isObject);
            FormatParameters formatParameters = labelFormatParameters(explode);

            String prefix = formatParameters.prefix.replace(PARAMETER_NAME_TOKEN, parameterName);

            String encoded;
            if (isObject && explode) {
                encoded = prefix + formatExploded(values, formatParameters.separator);
            } else {
                encoded = prefix + values.stream()
                    .collect(joining(formatParameters.separator));
            }

            return encoded;
        }

        private static FormatParameters labelFormatParameters(boolean explode) {
            return explode ? DEFAULT_LABEL_EXPLODED_PARAMETERS : DEFAULT_LABEL_FORMAT_PARAMETERS;
        }
    }

    private static class SimpleEncoder implements StyleEncoder {

        private final FormatParameters formatParameters;

        private SimpleEncoder(FormatParameters formatParameters) {
            this.formatParameters = formatParameters;
        }

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            List<String> values = toList(parameterValue, isObject);
            String prefix = formatParameters.prefix.replace(PARAMETER_NAME_TOKEN, parameterName);

            String encoded;
            if (isObject && explode) {
                encoded = prefix + formatExploded(values, formatParameters.separator);
            } else {
                encoded = prefix + values.stream()
                    .collect(joining(formatParameters.separator));
            }

            return encoded;
        }
    }

    private static class FormEncoder implements StyleEncoder {

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            List<String> values = toList(parameterValue, isObject);
            FormatParameters formatParameters = formFormatParameters(parameterName, explode,
                isObject);

            String encoded;
            if (isObject && explode) {
                encoded =
                    formatParameters.prefix + formatExploded(values, formatParameters.separator);
            } else {
                encoded = formatParameters.prefix + values.stream()
                    .collect(joining(formatParameters.separator));
            }

            return encoded;
        }

        private static FormatParameters formFormatParameters(String parameterName, boolean explode,
            boolean isObject) {
            if (explode) {
                if (isObject) {
                    return new FormatParameters("", "&");
                }
                return new FormatParameters(parameterName + "=", "&" + parameterName + "=");
            } else {
                return new FormatParameters(parameterName + "=", ",");
            }
        }
    }

    /**
     * This is an extension to the standard OpenAPI spec, that supports a common coding style for
     * deeply nested objects, where the json itself is serialized as string.
     */
    private static class DeepObjectEncoder implements StyleEncoder {

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            List<String> values = toList(parameterValue, isObject);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < values.size(); i += 2) {
                String key = values.get(i);
                String value = values.get(i + 1);
                builder.append("%s[%s]=%s".formatted(parameterName, key, value));
                builder.append("&");
            }

            if (!builder.isEmpty()) {
                builder.deleteCharAt(builder.length() - 1);
            }

            return builder.toString();
        }
    }

    private static class XEncodeAsJsonEncoder implements StyleEncoder {

        @Override
        public String encodeAccordingToSpec(String parameterName, Object parameterValue,
            boolean explode, boolean isObject) {
            if (parameterValue == null) {
                return "";
            }

            JsonNode jsonNode;

            // If it's a string, try parsing as JSON, fallback to URL decoding, as the user might have
            // encoded the content already.
            if (parameterValue instanceof String stringValue) {
                jsonNode = tryParseJson(stringValue);
                if (jsonNode == null) {
                    String decoded = decode(stringValue, StandardCharsets.UTF_8);
                    jsonNode = tryParseJson(decoded);
                    if (jsonNode == null) {
                        throw new IllegalArgumentException(format(
                            "Unable to convert string to JSON. Is this a valid JSON or URL-encoded JSON string?%n%s",
                            stringValue));
                    }
                }
            } else {
                // If not a string, try to convert object directly
                jsonNode = objectMapper.valueToTree(parameterValue);
            }

            try {
                String compactJson = objectMapper.writeValueAsString(jsonNode)
                    .replaceAll("\\R", "");

                // There is no other way to pass this into a query parameter, then URL encoding it.
                return parameterName + "=" + URLEncoder.encode(compactJson, StandardCharsets.UTF_8);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Unable to serialize object to JSON string.", e);
            }
        }

        /**
         * Try to parse the input as json. Silently returns null if input cannot be parsed to json.
         */
        private @Nullable JsonNode tryParseJson(String input) {
            try {
                return objectMapper.readTree(input);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
    }
}
