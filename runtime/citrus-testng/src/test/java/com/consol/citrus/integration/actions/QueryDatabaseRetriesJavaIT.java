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

package com.consol.citrus.integration.actions;

import javax.sql.DataSource;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.testng.TestNGCitrusSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ExecuteSQLAction.Builder.sql;
import static com.consol.citrus.actions.ExecuteSQLQueryAction.Builder.query;
import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.container.Parallel.Builder.parallel;
import static com.consol.citrus.container.RepeatOnErrorUntilTrue.Builder.repeatOnError;
import static com.consol.citrus.container.Sequence.Builder.sequential;

/**
 * @author Christoph Deppisch
 */
@Test
public class QueryDatabaseRetriesJavaIT extends TestNGCitrusSupport {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @CitrusTest
    public void sqlQueryRetries() {
        run(parallel().actions(
            sequential().actions(
                sql(dataSource)
                    .sqlResource("classpath:com/consol/citrus/integration/actions/script.sql"),
                repeatOnError()
                    .autoSleep(100).index("i").until("i = 5")
                    .actions(query(dataSource)
                        .statement("select COUNT(*) as customer_cnt from CUSTOMERS")
                        .validate("CUSTOMER_CNT", "0")
                )
            ),
            sequential().actions(
                sleep().milliseconds(300),
                sql(dataSource)
                    .statement("DELETE FROM CUSTOMERS")
            )
        ));
    }
}
