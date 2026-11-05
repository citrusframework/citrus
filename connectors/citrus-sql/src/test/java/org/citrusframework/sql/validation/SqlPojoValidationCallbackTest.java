/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.sql.validation;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * POJO mapping for result-set callbacks (T4): rows map to the caller type
 * case-insensitively, ANY row may satisfy the assertion, failures are loud.
 */
public class SqlPojoValidationCallbackTest {

    public static class OrderRow {
        private String status;
        private String orderName;
        private String orderType;
        private Timestamp created;
        private byte[] payload;
        private String note;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getOrderName() { return orderName; }
        public void setOrderName(String orderName) { this.orderName = orderName; }
        public String getOrderType() { return orderType; }
        public void setOrderType(String orderType) { this.orderType = orderType; }
        public Timestamp getCreated() { return created; }
        public void setCreated(Timestamp created) { this.created = created; }
        public byte[] getPayload() { return payload; }
        public void setPayload(byte[] payload) { this.payload = payload; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        @Override
        public String toString() {
            return "OrderRow{status='" + status + "'}";
        }
    }

    private final TestContext context = Mockito.mock(TestContext.class);

    private Map<String, Object> row(String status) {
        Map<String, Object> row = new HashMap<>();
        row.put("STATUS", status);
        row.put("orderName", "Mickey");
        row.put("ORDER_TYPE", "small");
        row.put("CREATED", Timestamp.valueOf("2026-10-08 12:00:00"));
        row.put("PAYLOAD", "binary".getBytes());
        row.put("NOTE", null);
        return row;
    }

    @Test
    public void mapsRowToPojo() {
        List<OrderRow> seen = new ArrayList<>();

        new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
            @Override
            protected void validate(OrderRow row, TestContext context) {
                seen.add(row);
            }
        }.validateSqlResultSet(Collections.singletonList(row("in_progress")), context);

        Assertions.assertEquals(1, seen.size());
        OrderRow pojo = seen.get(0);
        Assertions.assertEquals("in_progress", pojo.getStatus());
        Assertions.assertEquals("Mickey", pojo.getOrderName());
        Assertions.assertEquals("small", pojo.getOrderType());
        Assertions.assertEquals(Timestamp.valueOf("2026-10-08 12:00:00"), pojo.getCreated());
        Assertions.assertEquals("binary", new String(pojo.getPayload()));
        Assertions.assertNull(pojo.getNote());
    }

    @Test
    public void anyRowSatisfies() {
        List<Map<String, Object>> rows = List.of(row("started"), row("in_progress"), row("finished"));
        List<String> validated = new ArrayList<>();

        new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
            @Override
            protected void validate(OrderRow row, TestContext context) {
                validated.add(row.getStatus());
                if (!"in_progress".equals(row.getStatus())) {
                    throw new ValidationException("not the row: " + row.getStatus());
                }
            }
        }.validateSqlResultSet(rows, context);

        Assertions.assertEquals(List.of("started", "in_progress"), validated);
    }

    @Test
    public void assertionErrorAlsoCountsAsMismatch() {
        List<Map<String, Object>> rows = List.of(row("started"), row("finished"));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                    Assertions.assertEquals("in_progress", row.getStatus());
                }
            }.validateSqlResultSet(rows, context));
        Assertions.assertTrue(e.getMessage().contains("2 row(s)"));
    }

    @Test
    public void totalFailureListsRowsSeen() {
        List<Map<String, Object>> rows = List.of(row("started"), row("finished"));

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                    throw new ValidationException("never matches");
                }
            }.validateSqlResultSet(rows, context));
        Assertions.assertTrue(e.getMessage().contains("started"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("finished"), e.getMessage());
    }

    @Test
    public void emptyResultSetFailsNamingZeroRows() {
        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.emptyList(), context));
        Assertions.assertTrue(e.getMessage().contains("0 rows"), e.getMessage());
    }

    @Test
    public void listOverrideReceivesEmptyList() {
        boolean[] listCalled = {false};

        new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
            @Override
            public void validate(List<OrderRow> rows, TestContext context) {
                listCalled[0] = true;
                Assertions.assertTrue(rows.isEmpty());
            }

            @Override
            protected void validate(OrderRow row, TestContext context) {
            }
        }.validateSqlResultSet(Collections.emptyList(), context);

        Assertions.assertTrue(listCalled[0], "list-level override never received the empty list");
    }

    @Test
    public void unknownColumnFailsLoudly() {        Map<String, Object> badRow = row("in_progress");
        badRow.put("BOGUS", "x");

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(badRow), context));
        Assertions.assertTrue(e.getMessage().contains("BOGUS"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("STATUS"), e.getMessage());
    }

    public static class IntRow {
        private int count;

        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    public static class NoDefaultConstructor {
        private String status;

        NoDefaultConstructor(String status) { this.status = status; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Test
    public void nullTypeFailsFast() {
        NullPointerException e = Assertions.assertThrows(NullPointerException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(null) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            });
        Assertions.assertTrue(e.getMessage().contains("type"), e.getMessage());
    }

    @Test
    public void missingNoArgConstructorFailsLoudly() {
        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<NoDefaultConstructor>(NoDefaultConstructor.class) {
                @Override
                protected void validate(NoDefaultConstructor row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(row("in_progress")), context));
        Assertions.assertTrue(e.getMessage().contains("no-arg"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("NoDefaultConstructor"), e.getMessage());
    }

    @Test
    public void duplicateFoldedColumnsFail() {
        Map<String, Object> colliding = row("in_progress");
        colliding.put("status", "other");

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(colliding), context));
        Assertions.assertTrue(e.getMessage().contains("Duplicate"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("status"), e.getMessage());
    }

    @Test
    public void nullIntoPrimitiveFailsClearly() {
        Map<String, Object> row = new HashMap<>();
        row.put("COUNT", null);

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<IntRow>(IntRow.class) {
                @Override
                protected void validate(IntRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(row), context));
        Assertions.assertTrue(e.getMessage().contains("NULL"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("primitive"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("count"), e.getMessage());
    }

    @Test
    public void conversionMismatchNamesColumnRowAndProperty() {
        Map<String, Object> row = new HashMap<>();
        row.put("COUNT", "not-a-number");

        ValidationException e = Assertions.assertThrows(ValidationException.class, () ->
            new SqlPojoValidationCallback<IntRow>(IntRow.class) {
                @Override
                protected void validate(IntRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(row), context));
        Assertions.assertTrue(e.getMessage().contains("COUNT"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("row 0"), e.getMessage());
        Assertions.assertTrue(e.getMessage().contains("count"), e.getMessage());
    }
}
