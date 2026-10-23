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

import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlResultTest {

    @Test
    public void shouldPreserveDocumentOrderAndTotalCount() {
        NoSqlValue first = NoSqlValue.object(Map.of("id", NoSqlValue.string("1")));
        NoSqlValue second = NoSqlValue.object(Map.of("id", NoSqlValue.string("2")));

        QueryResult result = QueryResult.of(List.of(first, second), 2);

        assertEquals(result.documents(), List.of(first, second));
        assertEquals(result.totalCount(), OptionalLong.of(2));
    }

    @Test
    public void shouldRejectMutatingQueryDocuments() {
        QueryResult result = QueryResult.of(List.of(NoSqlValue.string("value")));

        expectThrows(UnsupportedOperationException.class,
                () -> result.documents().add(NoSqlValue.string("other")));
        assertEquals(result.totalCount(), OptionalLong.empty());
    }

    @Test
    public void shouldKeepAbsentWriteCountersAbsent() {
        WriteResult result = WriteResult.inserted(1, List.of(NoSqlValue.string("id-1")));

        assertEquals(result.inserted(), OptionalLong.of(1));
        assertTrue(result.matched().isEmpty());
        assertTrue(result.modified().isEmpty());
        assertTrue(result.deleted().isEmpty());
        assertEquals(result.generatedIds(), List.of(NoSqlValue.string("id-1")));
    }

    @Test
    public void shouldReportZeroCountersAsPresent() {
        WriteResult result = WriteResult.deleted(0);

        assertEquals(result.deleted(), OptionalLong.of(0));
        assertFalse(result.deleted().isEmpty());
    }

    @Test
    public void shouldKeepNativeMetadataImmutable() {
        Map<String, NoSqlValue> metadata = new java.util.LinkedHashMap<>();
        metadata.put("command", NoSqlValue.string("mongodb.aggregate"));

        NativeResult result = new NativeResult("mongodb", NoSqlValue.array(List.of()), metadata);

        assertEquals(result.metadata().get("command"), NoSqlValue.string("mongodb.aggregate"));
        expectThrows(UnsupportedOperationException.class,
                () -> result.metadata().put("other", NoSqlValue.string("value")));
    }
}
