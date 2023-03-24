/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.integration;

import javax.sql.DataSource;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecuteSQLAction.Builder.sql;
import static org.citrusframework.actions.ExecuteSQLQueryAction.Builder.query;
import static org.citrusframework.container.FinallySequence.Builder.doFinally;

/**
 * @author Christoph Deppisch
 */
@Test
public class ValidateSqlResultSetJavaIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @CitrusTest
    public void executeSQLAction() {
        variable("rowsCount", "2");
        variable("rtag", "requestTag");
        variable("desc", "Migrate");

        run(doFinally()
                .actions(sql(dataSource).statement("DELETE FROM ORDERS")));

        run(sql(dataSource)
            .statement("INSERT INTO ORDERS VALUES(1, 'requestTag', 'conversationId', 'creation_date', 'Migrate')")
            .statement("INSERT INTO ORDERS VALUES(2, 'requestTag', 'conversationId', 'creation_date', NULL)"));

        run(query(dataSource)
            .statement("SELECT REQUEST_TAG AS RTAG, DESCRIPTION AS DESC FROM ORDERS")
            .validateScript("assert rows.size() == ${rowsCount}\n" +
                    "assert rows[0].RTAG == '${rtag}'\n" +
                    "assert rows[0].DESC == '${desc}'\n", ScriptTypes.GROOVY));
    }
}
