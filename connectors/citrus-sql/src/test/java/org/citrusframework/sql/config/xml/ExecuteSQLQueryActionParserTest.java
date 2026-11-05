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

package org.citrusframework.sql.config.xml;

import org.citrusframework.sql.actions.ExecuteSQLQueryAction;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExecuteSQLQueryActionParserTest extends AbstractActionParserTest<ExecuteSQLQueryAction> {

    @Test
    public void testSQLActionParser() {
        assertActionCount(5);
        assertActionClassAndName(ExecuteSQLQueryAction.class, "sqlQuery:testDataSource");

        // 1st action
        ExecuteSQLQueryAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(2, action.getStatements().size());
        Assertions.assertEquals("select A, B, C from D where E='${id}'", action.getStatements().get(0));
        Assertions.assertEquals("select COUNT(F) as cnt_f from G", action.getStatements().get(1));
        Assertions.assertEquals(4, action.getControlResultSet().size());
        Assertions.assertEquals(1, action.getControlResultSet().get("A").size());
        Assertions.assertEquals("a", action.getControlResultSet().get("A").get(0));
        Assertions.assertEquals(1, action.getControlResultSet().get("B").size());
        Assertions.assertEquals("b", action.getControlResultSet().get("B").get(0));
        Assertions.assertEquals(1, action.getControlResultSet().get("C").size());
        Assertions.assertEquals("NULL", action.getControlResultSet().get("C").get(0));
        Assertions.assertEquals("${count}", action.getControlResultSet().get("CNT_F").get(0));
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());
        Assertions.assertNull(action.getScriptValidationContext());
        Assertions.assertEquals(0, action.getExtractVariables().size());

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNotNull(action.getSqlResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/sql/actions/test-sql-query-statements.sql", action.getSqlResourcePath());
        Assertions.assertEquals(0, action.getStatements().size());
        Assertions.assertEquals(1, action.getControlResultSet().size());
        Assertions.assertEquals("1", action.getControlResultSet().get("foo").get(0));
        Assertions.assertNotNull(action.getTransactionManager());
        Assertions.assertEquals(beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class), action.getTransactionManager());
        Assertions.assertEquals("5000", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_READ_COMMITTED", action.getTransactionIsolationLevel());
        Assertions.assertNull(action.getScriptValidationContext());
        Assertions.assertEquals(0, action.getExtractVariables().size());

        // 3rd action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("select A as A_COLUMN, B as B_COLUMN from C", action.getStatements().get(0));
        Assertions.assertEquals(2, action.getControlResultSet().size());
        Assertions.assertEquals(2, action.getControlResultSet().get("A_COLUMN").size());
        Assertions.assertEquals("a", action.getControlResultSet().get("A_COLUMN").get(0));
        Assertions.assertEquals("@ignore@", action.getControlResultSet().get("A_COLUMN").get(1));
        Assertions.assertEquals(2, action.getControlResultSet().get("B_COLUMN").size());
        Assertions.assertEquals("b", action.getControlResultSet().get("B_COLUMN").get(0));
        Assertions.assertEquals("NULL", action.getControlResultSet().get("B_COLUMN").get(1));
        Assertions.assertNull(action.getScriptValidationContext());
        Assertions.assertEquals(2, action.getExtractVariables().size());
        Assertions.assertEquals("a_values", action.getExtractVariables().get("A_COLUMN"));
        Assertions.assertEquals("b_values", action.getExtractVariables().get("B_COLUMN"));

        // 4th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("select A as A_COLUMN, B as B_COLUMN from C", action.getStatements().get(0));
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());

        Assertions.assertNotNull(action.getScriptValidationContext());
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext) action.getScriptValidationContext();
        Assertions.assertNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals("assert rows.size() == 2", scriptValidationContext.getValidationScript().trim());

        // 5th action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(1, action.getStatements().size());
        Assertions.assertEquals("select A as A_COLUMN, B as B_COLUMN from C", action.getStatements().get(0));
        Assertions.assertEquals(0, action.getControlResultSet().size());
        Assertions.assertEquals(0, action.getExtractVariables().size());

        Assertions.assertNotNull(action.getScriptValidationContext());
        scriptValidationContext = (DefaultScriptValidationContext) action.getScriptValidationContext();
        Assertions.assertNotNull(scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/script/example.groovy", scriptValidationContext.getValidationScriptResourcePath());
        Assertions.assertEquals("", scriptValidationContext.getValidationScript());
    }

    @Test
    public void testMissingDataSourceBeanRef() {
        BeanDefinitionStoreException e = Assertions.assertThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("failed"));
        Assertions.assertTrue(e.getCause().getMessage().contains(
                "Missing proper data source reference"));
    }
}
