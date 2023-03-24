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

package org.citrusframework.integration.actions;

import javax.sql.DataSource;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ExecutePLSQLAction.Builder.plsql;

/**
 * @author Christoph Deppisch
 */
@Test
public class ExecutePLSQLJavaIT extends TestNGCitrusSpringSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @CitrusTest
    public void executePLSQLAction() {
        run(plsql(dataSource)
            .sqlResource("classpath:org/citrusframework/integration/actions/plsql.sql")
            .ignoreErrors(true));

        run(plsql(dataSource)
            .sqlScript("BEGIN\n" +
                            "EXECUTE IMMEDIATE 'create or replace function test (v_id in number) return number is\n" +
                              "begin\n" +
                               "if v_id  is null then\n" +
                                "return 0;\n" +
                                "end if;\n" +
                                "return v_id;\n" +
                              "end;';\n" +
                        "END;\n" +
                        "/")
            .ignoreErrors(true));
    }
}
