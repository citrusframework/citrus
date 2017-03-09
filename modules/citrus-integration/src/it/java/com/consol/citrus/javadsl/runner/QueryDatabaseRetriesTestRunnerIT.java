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
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import javax.sql.DataSource;

/**
 * @author Christoph Deppisch
 */
@Test
public class QueryDatabaseRetriesTestRunnerIT extends TestNGCitrusTestRunner {
    
    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;
    
    @CitrusTest
    public void sqlQueryRetries() {
        parallel().actions(
            sequential().actions(
                sql(builder -> builder.dataSource(dataSource)
                        .sqlResource("classpath:com/consol/citrus/actions/script.sql")),
                repeatOnError().autoSleep(100).index("i").until("i = 5")
                    .actions(
                        query(builder -> builder.dataSource(dataSource)
                                .statement("select COUNT(*) as customer_cnt from CUSTOMERS")
                                .validate("CUSTOMER_CNT", "0"))
                )
            ),
            sequential().actions(
                sleep(300),
                sql(builder -> builder.dataSource(dataSource)
                        .statement("DELETE FROM CUSTOMERS"))
            )
        );
    }
}