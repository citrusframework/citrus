/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.javadsl.runner;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.builder.ExecutePLSQLBuilder;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import javax.sql.DataSource;

/**
 * @author Christoph Deppisch
 */
@Test
public class ExecutePLSQLTestRunnerITest extends TestNGCitrusTestRunner {
    
    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;
    
    @CitrusTest
    public void executePLSQLAction() {
        plsql(new BuilderSupport<ExecutePLSQLBuilder>() {
            @Override
            public void configure(ExecutePLSQLBuilder builder) {
                builder.dataSource(dataSource)
                        .sqlResource("classpath:com/consol/citrus/actions/plsql.sql")
                        .ignoreErrors(true);
            }
        });
        
        plsql(new BuilderSupport<ExecutePLSQLBuilder>() {
            @Override
            public void configure(ExecutePLSQLBuilder builder) {
                builder.dataSource(dataSource)
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
                        .ignoreErrors(true);
            }
        });
    }
}