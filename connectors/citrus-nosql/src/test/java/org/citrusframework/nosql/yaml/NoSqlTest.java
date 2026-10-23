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

package org.citrusframework.nosql.yaml;

import java.time.Duration;
import java.util.List;

import org.citrusframework.nosql.actions.NoSqlAction;
import org.citrusframework.nosql.actions.NoSqlActionSpec;
import org.citrusframework.nosql.actions.NoSqlExtraction;
import org.citrusframework.nosql.actions.NoSqlValidation;
import org.citrusframework.nosql.error.NoSqlErrorClass;
import org.citrusframework.nosql.error.NoSqlException;
import org.citrusframework.nosql.spi.NoSqlOperationKind;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class NoSqlTest {

    @Test
    public void shouldBuildQueryAction() {
        NoSql noSql = new NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("query");
        noSql.setTarget("orders");
        noSql.setFilter("{ \"orderId\": \"${orderId}\" }");
        noSql.setMaxResults(10);
        noSql.setRetryAttempts(3);
        noSql.setRetryInterval("PT0.5S");

        NoSql.Validate validate = new NoSql.Validate();
        validate.setPath("$.documents[0].status");
        validate.setValue("COMPLETED");
        noSql.setValidate(List.of(validate));

        NoSql.Extract extract = new NoSql.Extract();
        extract.setPath("$.documents[0].customerId");
        extract.setVariable("customerId");
        noSql.setExtract(List.of(extract));

        NoSqlAction action = noSql.build();
        NoSqlActionSpec spec = action.getSpec();

        assertEquals(spec.client(), "ordersDatabase");
        assertEquals(spec.kind(), NoSqlOperationKind.QUERY);
        assertEquals(spec.target(), "orders");
        assertEquals(spec.payload().content(), "{ \"orderId\": \"${orderId}\" }");
        assertEquals(spec.validations(), List.of(new NoSqlValidation("$.documents[0].status", "COMPLETED")));
        assertEquals(spec.extractions(), List.of(new NoSqlExtraction("$.documents[0].customerId", "customerId")));
        assertEquals(spec.maxResults(), java.util.OptionalInt.of(10));
        assertEquals(spec.retryPolicy().maxAttempts(), 3);
        assertEquals(spec.retryPolicy().interval(), Duration.ofMillis(500));
    }

    @Test
    public void shouldBuildNativeAction() {
        NoSql noSql = new NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("native");
        noSql.setCommand("mongodb.aggregate");
        noSql.setTarget("orders");
        noSql.setPayload("[{ \"$match\": { \"status\": \"COMPLETED\" } }]");

        NoSqlActionSpec spec = noSql.build().getSpec();

        assertEquals(spec.kind(), NoSqlOperationKind.NATIVE);
        assertEquals(spec.nativeCommand(), "mongodb.aggregate");
    }

    @Test
    public void shouldRejectUnsupportedOperation() {
        NoSql noSql = new NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("mapReduce");
        noSql.setTarget("orders");

        NoSqlException exception = expectThrows(NoSqlException.class, noSql::build);

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
        assertTrue(exception.getMessage().contains("mapReduce"));
    }

    @Test
    public void shouldRejectAmbiguousFilterAndPayload() {
        NoSql noSql = new NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("query");
        noSql.setTarget("orders");
        noSql.setFilter("{}");
        noSql.setPayload("{}");

        NoSqlException exception = expectThrows(NoSqlException.class, noSql::build);

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }

    @Test
    public void shouldRejectRetryIntervalWithoutRetryAttempts() {
        NoSql noSql = new NoSql();
        noSql.setClient("ordersDatabase");
        noSql.setOperation("query");
        noSql.setTarget("orders");
        noSql.setRetryInterval("200");

        NoSqlException exception = expectThrows(NoSqlException.class, noSql::build);

        assertEquals(exception.getErrorClass(), NoSqlErrorClass.CONFIGURATION);
    }
}
