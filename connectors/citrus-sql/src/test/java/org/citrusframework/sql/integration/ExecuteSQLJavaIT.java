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

package org.citrusframework.sql.integration;

import javax.sql.DataSource;

import org.citrusframework.TestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@CitrusSpringSupport
@Isolated("ITs share one HSQLDB in-memory database (jdbc:hsqldb:mem:test)")
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class ExecuteSQLJavaIT implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void executeSQLAction(@CitrusResource TestCaseRunner runner) {
        runner.variable("rowsCount", "0");
        runner.variable("customerId", "1");

        runner.run(sql().dataSource(dataSource)
            .sqlResource("classpath:org/citrusframework/sql/integration/script.sql"));

        runner.run(query().dataSource(dataSource)
            .statement("select NAME from CUSTOMERS where CUSTOMER_ID='${customerId}'")
            .statement("select COUNT(1) as overall_cnt from ERRORS")
            .statement("select ORDER_ID from ORDERS where DESCRIPTION LIKE 'Migrate%'")
            .statement("select DESCRIPTION from ORDERS where ORDER_ID = 2")
            .validate("ORDER_ID", "1")
            .validate("NAME", "Christoph")
            .validate("OVERALL_CNT", "${rowsCount}")
            .validate("DESCRIPTION", "NULL"));

        runner.run(query().dataSource(dataSource)
            .sqlResource("classpath:org/citrusframework/sql/integration/query-script.sql")
            .validate("ORDER_ID", "1")
            .validate("NAME", "Christoph")
            .validate("OVERALL_CNT", "${rowsCount}")
            .validate("DESCRIPTION", "NULL"));

        runner.run(query().dataSource(dataSource)
            .statement("select REQUEST_TAG as RTAG, DESCRIPTION as DESC from ORDERS")
            .validate("RTAG", "requestTag", "@ignore@")
            .validate("DESC", "Migrate")
            .validate("DESC", "Migrate", "NULL")
            .extract("RTAG", "tags")
            .extract("DESC", "description"));

        runner.run(sql().dataSource(dataSource)
            .statement("DELETE FROM CUSTOMERS"));

        runner.run(query().dataSource(dataSource)
            .statement("select DESCRIPTION as desc from ORDERS where ORDER_ID = 2")
            .validate("DESC", ""));
    }
}
