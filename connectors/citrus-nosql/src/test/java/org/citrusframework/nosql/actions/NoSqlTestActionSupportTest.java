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

package org.citrusframework.nosql.actions;

import org.citrusframework.TestAction;
import org.citrusframework.api.actions.nosql.NoSqlActionBuilder;
import org.citrusframework.dsl.nosql.NoSqlTestActionSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class NoSqlTestActionSupportTest implements NoSqlTestActionSupport {

    @Test
    public void shouldLookupNoSqlActionBuilder() {
        NoSqlActionBuilder<?, ?> builder = nosql();

        assertTrue(builder instanceof NoSqlAction.Builder);
    }

    @Test
    public void shouldBuildActionFromJavaDsl() {
        TestAction action = nosql()
                .client("ordersDatabase")
                .query()
                .target("orders")
                .filter("{ \"orderId\": \"${orderId}\" }")
                .validate("$.documents[0].status", "COMPLETED")
                .extract("$.documents[0].customerId", "customerId")
                .maxResults(10)
                .build();

        assertTrue(action instanceof NoSqlAction);
        NoSqlActionSpec spec = ((NoSqlAction) action).getSpec();
        assertEquals(spec.client(), "ordersDatabase");
        assertEquals(spec.target(), "orders");
        assertEquals(spec.maxResults(), java.util.OptionalInt.of(10));
        assertEquals(spec.validations().size(), 1);
        assertEquals(spec.extractions().size(), 1);
    }
}
