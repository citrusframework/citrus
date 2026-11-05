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
public class ExecutePLSQLJavaIT implements TestActionSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @Test
    @CitrusTest
    public void executePLSQLAction(@CitrusResource TestCaseRunner runner) {
        runner.run(plsql().dataSource(dataSource)
            .sqlResource("classpath:org/citrusframework/sql/integration/plsql.sql")
            .ignoreErrors(true));

        runner.run(plsql().dataSource(dataSource)
            .sqlScript("""
                    BEGIN
                    EXECUTE IMMEDIATE 'create or replace function test(v_id in number)
                        return number is
                        begin
                        if v_id  is null then
                                            return 0;
                        end if;
                        return v_id;
                        end;';
                    END;
                    /""")
            .ignoreErrors(true));
    }
}
