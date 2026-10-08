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

package org.citrusframework.sql.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.sql.validation.SqlPojoValidationCallback;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Plain-Java result-set validation against a real database (T5): the validator from
 * citrus#619 runs without any script, POJOs map end to end, and the new branches
 * compose with the pre-existing validation paths.
 */
@Test
public class SqlResultSetValidatorJavaIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    public static class OrderDescription {
        private String description;

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return "OrderDescription{description='" + description + "'}";
        }
    }

    private void seed() {
        run(sql().dataSource(dataSource)
            .sqlResource("classpath:org/citrusframework/sql/integration/script.sql"));
    }

    private static boolean containsCause(Throwable throwable, String message) {
        while (throwable != null) {
            if (throwable.getMessage() != null && throwable.getMessage().contains(message)) {
                return true;
            }
            throwable = throwable.getCause();
        }
        return false;
    }

    @CitrusTest
    public void plainValidatorRunsWithoutScript() {
        seed();

        List<Map<String, Object>> captured = new ArrayList<>();

        run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
            .validator((resultSet, context) -> captured.addAll(resultSet)));

        Assert.assertEquals(captured.size(), 1);
        Assert.assertEquals(String.valueOf(captured.get(0).get("DESCRIPTION")), "Migrate");
    }

    @CitrusTest
    public void failingPlainValidatorFailsTest() {
        seed();

        try {
            run(query().dataSource(dataSource)
                .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
                .validator((resultSet, context) -> {
                    throw new ValidationException("always fails");
                }));
        } catch (Exception e) {
            Assert.assertTrue(containsCause(e, "always fails"),
                    "expected the validator failure in the cause chain, got: " + e);
            return;
        }

        Assert.fail("Expected ValidationException from failing plain validator");
    }

    @CitrusTest
    public void pojoCallbackEndToEnd() {
        seed();

        run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS order by ORDER_ID")
            .validator(new SqlPojoValidationCallback<OrderDescription>(OrderDescription.class) {
                @Override
                protected void validate(OrderDescription row, TestContext context) {
                    Assert.assertEquals(row.getDescription(), "Migrate");
                }
            }));
    }

    @CitrusTest
    public void plainValidatorSeesMultiStatementUnion() {
        seed();

        List<Map<String, Object>> captured = new ArrayList<>();

        run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS")
            .statement("select NAME from CUSTOMERS")
            .validator((resultSet, context) -> captured.addAll(resultSet)));

        Assert.assertEquals(captured.size(), 3);
    }

    @CitrusTest
    public void plainValidatorComposesWithControlResultSet() {
        seed();

        boolean[] called = {false};

        run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
            .validate("DESCRIPTION", "Migrate")
            .validator((resultSet, context) -> called[0] = true));

        Assert.assertTrue(called[0], "plain validator did not run alongside control validation");
    }
}
