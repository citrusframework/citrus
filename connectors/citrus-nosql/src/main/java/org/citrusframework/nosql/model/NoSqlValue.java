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

package org.citrusframework.nosql.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;

/**
 * Canonical, provider independent value model for NoSQL payloads and results.
 *
 * <p>The model distinguishes a missing value from an explicit {@code null} value. Providers must
 * use {@link #missing()} for absent fields and {@link #nullValue()} for fields that are present
 * with a null value. The distinction is preserved through validation and extraction.</p>
 *
 * <p>Values never expose provider SDK types. Provider specific types such as a MongoDB
 * {@code ObjectId} or {@code Decimal128} are represented by {@link ProviderValue} with an explicit
 * provider identifier and type name.</p>
 */
public sealed interface NoSqlValue {

    /**
     * Convert to a JSON compatible object model consisting of {@link Map}, {@link List},
     * {@link String}, {@link Boolean}, {@link BigDecimal} and {@code null} values.
     * Missing object entries are omitted, explicit null values are kept.
     */
    Object toJsonCompatible();

    /**
     * Render this value as plain text. Objects and arrays render as JSON, strings render without
     * quotes and explicit null values render as {@code null}.
     */
    default String render() {
        return JsonRendering.render(this);
    }

    /**
     * Whether this value represents an absent field.
     */
    default boolean isMissing() {
        return this instanceof MissingValue;
    }

    /**
     * Whether this value represents an explicit null value.
     */
    default boolean isNull() {
        return this instanceof NullValue;
    }

    static NoSqlValue string(String value) {
        return new StringValue(value);
    }

    static NoSqlValue bool(boolean value) {
        return new BooleanValue(value);
    }

    static NoSqlValue number(long value) {
        return new NumberValue(BigDecimal.valueOf(value));
    }

    static NoSqlValue number(double value) {
        return new NumberValue(BigDecimal.valueOf(value));
    }

    static NoSqlValue number(BigDecimal value) {
        return new NumberValue(value);
    }

    static NoSqlValue object(Map<String, NoSqlValue> values) {
        return new ObjectValue(values);
    }

    static NoSqlValue array(List<NoSqlValue> values) {
        return new ArrayValue(values);
    }

    static NoSqlValue array(NoSqlValue... values) {
        return new ArrayValue(Arrays.asList(values));
    }

    static NoSqlValue binary(byte[] value) {
        return new BinaryValue(value);
    }

    static NoSqlValue temporal(String kind, String text) {
        return new TemporalValue(kind, text);
    }

    static NoSqlValue provider(String providerId, String typeName, String representation) {
        return new ProviderValue(providerId, typeName, representation);
    }

    static NoSqlValue nullValue() {
        return NullValue.INSTANCE;
    }

    static NoSqlValue missing() {
        return MissingValue.INSTANCE;
    }

    /**
     * Convert a plain Java object to the canonical model. Supported types are {@link NoSqlValue},
     * {@link String}, {@link Boolean}, {@link Number}, {@link Map}, {@link Iterable}, arrays and
     * {@code null}.
     */
    @SuppressWarnings("unchecked")
    static NoSqlValue of(Object value) {
        if (value == null) {
            return nullValue();
        } else if (value instanceof NoSqlValue noSqlValue) {
            return noSqlValue;
        } else if (value instanceof String stringValue) {
            return string(stringValue);
        } else if (value instanceof Boolean booleanValue) {
            return bool(booleanValue);
        } else if (value instanceof BigDecimal bigDecimal) {
            return number(bigDecimal);
        } else if (value instanceof BigInteger bigInteger) {
            return number(new BigDecimal(bigInteger));
        } else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return number(((Number) value).longValue());
        } else if (value instanceof Number numberValue) {
            return number(numberValue.doubleValue());
        } else if (value instanceof byte[] bytes) {
            return binary(bytes);
        } else if (value instanceof Map<?, ?> map) {
            Map<String, NoSqlValue> converted = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> converted.put(String.valueOf(key), of(entryValue)));
            return object(converted);
        } else if (value instanceof Iterable<?> iterable) {
            List<NoSqlValue> converted = new ArrayList<>();
            iterable.forEach(entry -> converted.add(of(entry)));
            return array(converted);
        } else if (value instanceof Object[] array) {
            return array(Arrays.stream(array).map(NoSqlValue::of).toList());
        }

        throw new NoSqlException(NoSqlErrorClass.INVALID_PAYLOAD,
                "Unsupported value type: " + value.getClass().getName());
    }

    record StringValue(String value) implements NoSqlValue {

        public StringValue {
            Objects.requireNonNull(value, "String value must not be null");
        }

        @Override
        public Object toJsonCompatible() {
            return value;
        }
    }

    record BooleanValue(boolean value) implements NoSqlValue {

        @Override
        public Object toJsonCompatible() {
            return value;
        }
    }

    record NumberValue(BigDecimal value) implements NoSqlValue {

        public NumberValue {
            Objects.requireNonNull(value, "Number value must not be null");
            value = value.stripTrailingZeros();
        }

        @Override
        public Object toJsonCompatible() {
            return new BigDecimal(value.toPlainString());
        }
    }

    record ObjectValue(Map<String, NoSqlValue> values) implements NoSqlValue {

        public ObjectValue {
            Objects.requireNonNull(values, "Object values must not be null");
            values = new LinkedHashMap<>(values);
            values.values().forEach(Objects::requireNonNull);
        }

        @Override
        public Map<String, NoSqlValue> values() {
            return java.util.Collections.unmodifiableMap(values);
        }

        @Override
        public Object toJsonCompatible() {
            Map<String, Object> converted = new LinkedHashMap<>();
            values.forEach((key, entryValue) -> {
                if (!entryValue.isMissing()) {
                    converted.put(key, entryValue.toJsonCompatible());
                }
            });
            return converted;
        }
    }

    record ArrayValue(List<NoSqlValue> values) implements NoSqlValue {

        public ArrayValue {
            Objects.requireNonNull(values, "Array values must not be null");
            values = List.copyOf(values);
            values.forEach(Objects::requireNonNull);
        }

        @Override
        public Object toJsonCompatible() {
            List<Object> converted = new ArrayList<>(values.size());
            values.forEach(entry -> converted.add(entry.toJsonCompatible()));
            return converted;
        }
    }

    record BinaryValue(byte[] value) implements NoSqlValue {

        public BinaryValue {
            Objects.requireNonNull(value, "Binary value must not be null");
            value = Arrays.copyOf(value, value.length);
        }

        @Override
        public byte[] value() {
            return Arrays.copyOf(value, value.length);
        }

        @Override
        public Object toJsonCompatible() {
            return Base64.getEncoder().encodeToString(value);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof BinaryValue binaryValue && Arrays.equals(value, binaryValue.value);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }

        @Override
        public String toString() {
            return "BinaryValue[value=" + Base64.getEncoder().encodeToString(value) + "]";
        }
    }

    /**
     * Temporal value with a canonical kind, for example {@code instant}, {@code local-date},
     * {@code local-time}, {@code local-date-time}, {@code offset-date-time} or
     * {@code zoned-date-time}, and an ISO text representation.
     */
    record TemporalValue(String kind, String text) implements NoSqlValue {

        public TemporalValue {
            Objects.requireNonNull(kind, "Temporal kind must not be null");
            Objects.requireNonNull(text, "Temporal text must not be null");
        }

        @Override
        public Object toJsonCompatible() {
            return text;
        }
    }

    /**
     * Provider specific value that has no canonical counterpart, for example a MongoDB ObjectId.
     * The representation is a stable text form such as a hex string.
     */
    record ProviderValue(String providerId, String typeName, String representation) implements NoSqlValue {

        public ProviderValue {
            Objects.requireNonNull(providerId, "Provider id must not be null");
            Objects.requireNonNull(typeName, "Provider type name must not be null");
            Objects.requireNonNull(representation, "Provider value representation must not be null");
        }

        @Override
        public Object toJsonCompatible() {
            return representation;
        }

        @Override
        public String render() {
            return representation;
        }
    }

    record NullValue() implements NoSqlValue {

        static final NullValue INSTANCE = new NullValue();

        @Override
        public Object toJsonCompatible() {
            return null;
        }
    }

    record MissingValue() implements NoSqlValue {

        static final MissingValue INSTANCE = new MissingValue();

        @Override
        public Object toJsonCompatible() {
            return null;
        }

        @Override
        public String render() {
            return "";
        }
    }

    /**
     * Internal JSON rendering of canonical values.
     */
    final class JsonRendering {

        private JsonRendering() {
        }

        static String render(NoSqlValue value) {
            if (value instanceof StringValue stringValue) {
                return stringValue.value();
            } else if (value instanceof BooleanValue booleanValue) {
                return String.valueOf(booleanValue.value());
            } else if (value instanceof NumberValue numberValue) {
                return numberValue.value().toPlainString();
            } else if (value instanceof BinaryValue binaryValue) {
                return Base64.getEncoder().encodeToString(binaryValue.value);
            } else if (value instanceof TemporalValue temporalValue) {
                return temporalValue.text();
            } else if (value instanceof ProviderValue providerValue) {
                return providerValue.representation();
            } else if (value instanceof NullValue) {
                return "null";
            } else if (value instanceof MissingValue) {
                return "";
            } else if (value instanceof ObjectValue objectValue) {
                StringBuilder builder = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<String, NoSqlValue> entry : objectValue.values().entrySet()) {
                    if (entry.getValue().isMissing()) {
                        continue;
                    }
                    if (!first) {
                        builder.append(',');
                    }
                    first = false;
                    builder.append(quote(entry.getKey())).append(':').append(json(entry.getValue()));
                }
                return builder.append('}').toString();
            } else if (value instanceof ArrayValue arrayValue) {
                StringBuilder builder = new StringBuilder("[");
                for (int i = 0; i < arrayValue.values().size(); i++) {
                    if (i > 0) {
                        builder.append(',');
                    }
                    builder.append(json(arrayValue.values().get(i)));
                }
                return builder.append(']').toString();
            }

            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }

        private static String json(NoSqlValue value) {
            if (value instanceof StringValue stringValue) {
                return quote(stringValue.value());
            } else if (value instanceof NullValue) {
                return "null";
            } else if (value instanceof MissingValue) {
                return "null";
            }

            return render(value);
        }

        private static String quote(String text) {
            StringBuilder builder = new StringBuilder(text.length() + 2).append('"');
            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                switch (character) {
                    case '"' -> builder.append("\\\"");
                    case '\\' -> builder.append("\\\\");
                    case '\b' -> builder.append("\\b");
                    case '\f' -> builder.append("\\f");
                    case '\n' -> builder.append("\\n");
                    case '\r' -> builder.append("\\r");
                    case '\t' -> builder.append("\\t");
                    default -> {
                        if (character < 0x20) {
                            builder.append(String.format("\\u%04x", (int) character));
                        } else {
                            builder.append(character);
                        }
                    }
                }
            }
            return builder.append('"').toString();
        }
    }

    /**
     * Create a binary value from text using UTF-8.
     */
    static NoSqlValue binaryOf(String value) {
        return binary(value.getBytes(StandardCharsets.UTF_8));
    }
}
