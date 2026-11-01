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

package org.citrusframework.validation.groovy.integration;

import javax.sql.DataSource;

import org.citrusframework.TestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.script.ScriptTypes;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated
@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class})
public class ValidateSqlResultSetJavaIT implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void executeSQLAction(@CitrusResource TestActionRunner runner) {
        runner.run(createVariable("rowsCount", "2"));
        runner.run(createVariable("rtag", "requestTag"));
        runner.run(createVariable("desc", "Migrate"));

        runner.run(doFinally()
                .actions(sql().dataSource(dataSource).statement("DELETE FROM ORDERS")));

        runner.run(sql().dataSource(dataSource)
            .statement("INSERT INTO ORDERS VALUES(1, 'requestTag', 'conversationId', 'creation_date', 'Migrate')")
            .statement("INSERT INTO ORDERS VALUES(2, 'requestTag', 'conversationId', 'creation_date', NULL)"));

        runner.run(query().dataSource(dataSource)
            .statement("SELECT REQUEST_TAG AS RTAG, DESCRIPTION AS DESC FROM ORDERS")
            .validateScript("""
                    assert rows.size() == ${rowsCount}
                    assert rows[0].RTAG == '${rtag}'
                    assert rows[0].DESC == '${desc}'
                    """, ScriptTypes.GROOVY));
    }
}
