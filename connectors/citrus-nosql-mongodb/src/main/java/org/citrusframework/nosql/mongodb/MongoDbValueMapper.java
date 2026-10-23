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

package org.citrusframework.nosql.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.bson.types.Symbol;
import org.citrusframework.nosql.model.NoSqlValue;

/**
 * Maps BSON values to the canonical NoSQL value model.
 *
 * <p>Values without a canonical counterpart stay explicit provider values. For example an
 * {@link ObjectId} becomes a provider value of type {@code objectId} and a {@link Decimal128} a
 * provider value of type {@code decimal128}. Neither is silently turned into a string.</p>
 */
public final class MongoDbValueMapper {

    public static final String PROVIDER_ID = "mongodb";

    public NoSqlValue map(Object value) {
        if (value == null) {
            return NoSqlValue.nullValue();
        } else if (value instanceof NoSqlValue noSqlValue) {
            return noSqlValue;
        } else if (value instanceof BsonValue bsonValue) {
            return mapBsonValue(bsonValue);
        } else if (value instanceof ObjectId objectId) {
            return NoSqlValue.provider(PROVIDER_ID, "objectId", objectId.toHexString());
        } else if (value instanceof Decimal128 decimal128) {
            return NoSqlValue.provider(PROVIDER_ID, "decimal128", decimal128.toString());
        } else if (value instanceof Binary binary) {
            return NoSqlValue.binary(binary.getData());
        } else if (value instanceof Code code) {
            return NoSqlValue.provider(PROVIDER_ID, "javascript", String.valueOf(code.getCode()));
        } else if (value instanceof Symbol symbol) {
            return NoSqlValue.provider(PROVIDER_ID, "symbol", symbol.getSymbol());
        } else if (value instanceof java.util.Date date) {
            return NoSqlValue.temporal("instant", date.toInstant().toString());
        } else if (value instanceof Double doubleValue && !Double.isFinite(doubleValue)) {
            return NoSqlValue.provider(PROVIDER_ID, "double", Double.toString(doubleValue));
        } else if (value instanceof Float floatValue && !Float.isFinite(floatValue)) {
            return NoSqlValue.provider(PROVIDER_ID, "double", Float.toString(floatValue));
        } else if (value instanceof Map<?, ?> map) {
            Map<String, NoSqlValue> values = new LinkedHashMap<>();
            map.forEach((key, entryValue) -> values.put(String.valueOf(key), map(entryValue)));
            return NoSqlValue.object(values);
        } else if (value instanceof Iterable<?> iterable) {
            List<NoSqlValue> values = new ArrayList<>();
            iterable.forEach(entry -> values.add(map(entry)));
            return NoSqlValue.array(values);
        }

        return NoSqlValue.of(value);
    }

    /**
     * Map a MongoDB document to a canonical object value.
     */
    public NoSqlValue map(Document document) {
        Map<String, NoSqlValue> values = new LinkedHashMap<>();
        document.forEach((key, value) -> values.put(key, map(value)));
        return NoSqlValue.object(values);
    }

    private NoSqlValue mapBsonValue(BsonValue value) {
        return switch (value.getBsonType()) {
            case OBJECT_ID -> NoSqlValue.provider(PROVIDER_ID, "objectId", value.asObjectId().getValue().toHexString());
            case DECIMAL128 -> NoSqlValue.provider(PROVIDER_ID, "decimal128", value.asDecimal128().getValue().toString());
            case STRING -> NoSqlValue.string(value.asString().getValue());
            case BOOLEAN -> NoSqlValue.bool(value.asBoolean().getValue());
            case INT32 -> NoSqlValue.number(value.asInt32().getValue());
            case INT64 -> NoSqlValue.number(value.asInt64().getValue());
            case DOUBLE -> {
                double doubleValue = value.asDouble().getValue();
                yield Double.isFinite(doubleValue)
                        ? NoSqlValue.number(doubleValue)
                        : NoSqlValue.provider(PROVIDER_ID, "double", Double.toString(doubleValue));
            }
            case BINARY -> NoSqlValue.binary(value.asBinary().getData());
            case DATE_TIME -> NoSqlValue.temporal("instant",
                    Instant.ofEpochMilli(value.asDateTime().getValue()).toString());
            case NULL -> NoSqlValue.nullValue();
            case ARRAY -> NoSqlValue.array(value.asArray().getValues().stream().map(this::mapBsonValue).toList());
            case DOCUMENT -> map(new Document(value.asDocument()));
            default -> NoSqlValue.provider(PROVIDER_ID, value.getBsonType().name().toLowerCase(Locale.ROOT), value.toString());
        };
    }
}
