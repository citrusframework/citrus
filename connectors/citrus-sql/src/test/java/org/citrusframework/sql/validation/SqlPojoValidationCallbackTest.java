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
import org.testng.Assert;
import org.testng.annotations.Test;

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

        Assert.assertEquals(seen.size(), 1);
        OrderRow pojo = seen.get(0);
        Assert.assertEquals(pojo.getStatus(), "in_progress");
        Assert.assertEquals(pojo.getOrderName(), "Mickey");
        Assert.assertEquals(pojo.getOrderType(), "small");
        Assert.assertEquals(pojo.getCreated(), Timestamp.valueOf("2026-10-08 12:00:00"));
        Assert.assertEquals(new String(pojo.getPayload()), "binary");
        Assert.assertNull(pojo.getNote());
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

        Assert.assertEquals(validated, List.of("started", "in_progress"));
    }

    @Test
    public void assertionErrorAlsoCountsAsMismatch() {
        List<Map<String, Object>> rows = List.of(row("started"), row("finished"));

        try {
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                    Assert.assertEquals(row.getStatus(), "in_progress");
                }
            }.validateSqlResultSet(rows, context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("2 row(s)"));
            return;
        }

        Assert.fail("Expected ValidationException when no row satisfies");
    }

    @Test
    public void totalFailureListsRowsSeen() {
        List<Map<String, Object>> rows = List.of(row("started"), row("finished"));

        try {
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                    throw new ValidationException("never matches");
                }
            }.validateSqlResultSet(rows, context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("started"), e.getMessage());
            Assert.assertTrue(e.getMessage().contains("finished"), e.getMessage());
            return;
        }

        Assert.fail("Expected ValidationException listing rows seen");
    }

    @Test
    public void emptyResultSetFailsNamingZeroRows() {
        try {
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.emptyList(), context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("0 rows"), e.getMessage());
            return;
        }

        Assert.fail("Expected ValidationException for empty result set");
    }

    @Test
    public void listOverrideReceivesEmptyList() {
        boolean[] listCalled = {false};

        new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
            @Override
            public void validate(List<OrderRow> rows, TestContext context) {
                listCalled[0] = true;
                Assert.assertTrue(rows.isEmpty());
            }

            @Override
            protected void validate(OrderRow row, TestContext context) {
            }
        }.validateSqlResultSet(Collections.emptyList(), context);

        Assert.assertTrue(listCalled[0], "list-level override never received the empty list");
    }

    @Test
    public void unknownColumnFailsLoudly() {
        Map<String, Object> badRow = row("in_progress");
        badRow.put("BOGUS", "x");

        try {
            new SqlPojoValidationCallback<OrderRow>(OrderRow.class) {
                @Override
                protected void validate(OrderRow row, TestContext context) {
                }
            }.validateSqlResultSet(Collections.singletonList(badRow), context);
        } catch (ValidationException e) {
            Assert.assertTrue(e.getMessage().contains("BOGUS"), e.getMessage());
            Assert.assertTrue(e.getMessage().contains("STATUS"), e.getMessage());
            return;
        }

        Assert.fail("Expected loud failure for unmappable column");
    }
}
