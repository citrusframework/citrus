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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.citrusframework.nosql.model.NoSqlValue;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MongoDbValueMapperTest {

    private final MongoDbValueMapper mapper = new MongoDbValueMapper();

    @Test
    public void shouldMapObjectIdToExplicitProviderValue() {
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");

        NoSqlValue value = mapper.map(objectId);

        assertEquals(value, NoSqlValue.provider("mongodb", "objectId", "507f1f77bcf86cd799439011"));
        assertEquals(value.render(), "507f1f77bcf86cd799439011");
    }

    @Test
    public void shouldMapDecimal128ToExplicitProviderValue() {
        NoSqlValue value = mapper.map(Decimal128.parse("12.50"));

        assertTrue(value instanceof NoSqlValue.ProviderValue);
        assertEquals(((NoSqlValue.ProviderValue) value).typeName(), "decimal128");
        assertEquals(value.render(), "12.50");
    }

    @Test
    public void shouldMapBinaryAndDates() {
        assertEquals(mapper.map(new Binary(new byte[] {1, 2, 3})), NoSqlValue.binary(new byte[] {1, 2, 3}));

        NoSqlValue date = mapper.map(Date.from(Instant.parse("2026-01-02T03:04:05Z")));
        assertEquals(date, NoSqlValue.temporal("instant", "2026-01-02T03:04:05Z"));
    }

    @Test
    public void shouldMapDocumentsRecursively() {
        Document document = new Document("orderId", "123")
                .append("amount", new BigDecimal("42.50"))
                .append("paid", true)
                .append("items", List.of(new Document("sku", "A-1")))
                .append("note", null);

        NoSqlValue value = mapper.map(document);

        assertTrue(value instanceof NoSqlValue.ObjectValue);
        NoSqlValue.ObjectValue object = (NoSqlValue.ObjectValue) value;
        assertEquals(object.values().get("orderId"), NoSqlValue.string("123"));
        assertEquals(object.values().get("amount"), NoSqlValue.number(new BigDecimal("42.50")));
        assertEquals(object.values().get("paid"), NoSqlValue.bool(true));
        assertEquals(object.values().get("items"), NoSqlValue.array(
                NoSqlValue.object(Map.of("sku", NoSqlValue.string("A-1")))));
        assertEquals(object.values().get("note"), NoSqlValue.nullValue());
    }

    @Test
    public void shouldKeepFieldOrder() {
        Document document = new Document("b", 2).append("a", 1);

        NoSqlValue.ObjectValue object = (NoSqlValue.ObjectValue) mapper.map(document);

        assertEquals(List.copyOf(object.values().keySet()), List.of("b", "a"));
    }

    @Test
    public void shouldMapNonFiniteDoublesToExplicitProviderValue() {
        assertEquals(mapper.map(Double.NaN), NoSqlValue.provider("mongodb", "double", "NaN"));
        assertEquals(mapper.map(Double.POSITIVE_INFINITY), NoSqlValue.provider("mongodb", "double", "Infinity"));
        assertEquals(mapper.map(new org.bson.BsonDouble(Double.NEGATIVE_INFINITY)),
                NoSqlValue.provider("mongodb", "double", "-Infinity"));
    }
}
