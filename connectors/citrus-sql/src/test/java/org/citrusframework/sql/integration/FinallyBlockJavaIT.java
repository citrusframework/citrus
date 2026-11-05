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
public class FinallyBlockJavaIT implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void finallyBlock(@CitrusResource TestCaseRunner runner) {
        runner.variable("orderId", "citrus:randomNumber(5)");

        runner.run(doFinally().actions(
                sql().dataSource(dataSource).statement("DELETE FROM ORDERS WHERE ORDER_ID='${orderId}'"),
                echo("ORDER deletion time: ${date}")
        ));

        runner.run(sql().dataSource(dataSource)
            .statement("INSERT INTO ORDERS (ORDER_ID, REQUEST_TAG, CONVERSATION_ID, CREATION_DATE) VALUES (${orderId},1,1,'citrus:currentDate(dd.MM.yyyy)')"));

        runner.run(query().dataSource(dataSource)
            .statement("SELECT CREATION_DATE FROM ORDERS WHERE ORDER_ID='${orderId}'")
            .extract("CREATION_DATE", "date"));

        runner.run(echo("ORDER creation time: ${date}"));
    }
}
