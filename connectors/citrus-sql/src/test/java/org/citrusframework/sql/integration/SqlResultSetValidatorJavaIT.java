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

import org.citrusframework.TestCaseRunner;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.sql.validation.SqlPojoValidationCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

/**
 * Plain-Java result-set validation against a real database (T5): the validator from
 * citrus#619 runs without any script, POJOs map end to end, and the new branches
 * compose with the pre-existing validation paths.
 */
@CitrusSpringSupport
@Isolated("ITs share one HSQLDB in-memory database (jdbc:hsqldb:mem:test)")
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class SqlResultSetValidatorJavaIT implements TestActionSupport {

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

    private void seed(TestCaseRunner runner) {
        runner.run(sql().dataSource(dataSource)
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

    @Test
    @CitrusTest
    public void plainValidatorRunsWithoutScript(@CitrusResource TestCaseRunner runner) {
        seed(runner);

        List<Map<String, Object>> captured = new ArrayList<>();

        runner.run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
            .validator((resultSet, context) -> captured.addAll(resultSet)));

        Assertions.assertEquals(1, captured.size());
        Assertions.assertEquals("Migrate", String.valueOf(captured.get(0).get("DESCRIPTION")));
    }

    @Test
    @CitrusTest
    public void failingPlainValidatorFailsTest(@CitrusResource TestCaseRunner runner) {
        seed(runner);

        Exception e = Assertions.assertThrows(Exception.class, () ->
            runner.run(query().dataSource(dataSource)
                .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
                .validator((resultSet, context) -> {
                    throw new ValidationException("always fails");
                })));
        Assertions.assertTrue(containsCause(e, "always fails"),
                "expected the validator failure in the cause chain, got: " + e);
    }

    @Test
    @CitrusTest
    public void pojoCallbackEndToEnd(@CitrusResource TestCaseRunner runner) {
        seed(runner);

        runner.run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS order by ORDER_ID")
            .validator(new SqlPojoValidationCallback<OrderDescription>(OrderDescription.class) {
                @Override
                protected void validate(OrderDescription row, TestContext context) {
                    Assertions.assertEquals("Migrate", row.getDescription());
                }
            }));
    }

    @Test
    @CitrusTest
    public void plainValidatorSeesMultiStatementUnion(@CitrusResource TestCaseRunner runner) {
        seed(runner);

        List<Map<String, Object>> captured = new ArrayList<>();

        runner.run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS")
            .statement("select NAME from CUSTOMERS")
            .validator((resultSet, context) -> captured.addAll(resultSet)));

        Assertions.assertEquals(3, captured.size());
    }

    @Test
    @CitrusTest
    public void plainValidatorComposesWithControlResultSet(@CitrusResource TestCaseRunner runner) {
        seed(runner);

        boolean[] called = {false};

        runner.run(query().dataSource(dataSource)
            .statement("select DESCRIPTION from ORDERS where ORDER_ID=1")
            .validate("DESCRIPTION", "Migrate")
            .validator((resultSet, context) -> called[0] = true));

        Assertions.assertTrue(called[0], "plain validator did not run alongside control validation");
    }
}
