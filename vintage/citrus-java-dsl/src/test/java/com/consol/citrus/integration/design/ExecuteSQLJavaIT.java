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

package com.consol.citrus.integration.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.script.ScriptTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import javax.sql.DataSource;

/**
 * @author Christoph Deppisch
 */
@Test
public class ExecuteSQLJavaIT extends TestNGCitrusTestDesigner {

    @Autowired
    @Qualifier("testDataSource")
    private DataSource dataSource;

    @CitrusTest
    public void executeSQLAction() {
        variable("rowsCount", "0");
        variable("customerId", "1");

        sql(dataSource)
            .sqlResource("classpath:com/consol/citrus/actions/script.sql");

        query(dataSource)
            .statement("select NAME from CUSTOMERS where CUSTOMER_ID='${customerId}'")
            .statement("select COUNT(1) as overall_cnt from ERRORS")
            .statement("select ORDER_ID from ORDERS where DESCRIPTION LIKE 'Migrate%'")
            .statement("select DESCRIPTION from ORDERS where ORDER_ID = 2")
            .validate("ORDER_ID", "1")
            .validate("NAME", "Christoph")
            .validate("OVERALL_CNT", "${rowsCount}")
            .validate("DESCRIPTION", "NULL");

        query(dataSource)
            .sqlResource("classpath:com/consol/citrus/actions/query-script.sql")
            .validate("ORDER_ID", "1")
            .validate("NAME", "Christoph")
            .validate("OVERALL_CNT", "${rowsCount}")
            .validate("DESCRIPTION", "NULL");

        query(dataSource)
            .statement("select REQUEST_TAG as RTAG, DESCRIPTION as DESC from ORDERS")
            .validate("RTAG", "requestTag", "@ignore@")
            .validate("DESC", "Migrate")
            .validate("DESC", "Migrate", "NULL")
            .extract("RTAG", "tags")
            .extract("DESC", "description");

        sql(dataSource)
            .statement("DELETE FROM CUSTOMERS");

        query(dataSource)
            .statement("select DESCRIPTION as desc from ORDERS where ORDER_ID = 2")
            .validate("DESC", "");

        query(dataSource)
            .statement("select REQUEST_TAG as RTAG, DESCRIPTION as DESC from ORDERS")
            .validateScript("assert rows.size() == 2\n" +
                    "assert rows[0].RTAG == 'requestTag'\n" +
                    "assert rows[0].DESC == 'Migrate'\n", ScriptTypes.GROOVY);
    }
}
