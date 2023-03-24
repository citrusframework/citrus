/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.config.xml;

import org.citrusframework.actions.ExecuteSQLQueryAction;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLQueryActionParserTest extends AbstractActionParserTest<ExecuteSQLQueryAction> {

    @Test
    public void testSQLActionParser() {
        assertActionCount(5);
        assertActionClassAndName(ExecuteSQLQueryAction.class, "sqlQuery:testDataSource");
        
        // 1st action
        ExecuteSQLQueryAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().get(0), "select A, B, C from D where E='${id}'");
        Assert.assertEquals(action.getStatements().get(1), "select COUNT(F) as cnt_f from G");
        Assert.assertEquals(action.getControlResultSet().size(), 4);
        Assert.assertEquals(action.getControlResultSet().get("A").size(), 1);
        Assert.assertEquals(action.getControlResultSet().get("A").get(0), "a");
        Assert.assertEquals(action.getControlResultSet().get("B").size(), 1);
        Assert.assertEquals(action.getControlResultSet().get("B").get(0), "b");
        Assert.assertEquals(action.getControlResultSet().get("C").size(), 1);
        Assert.assertEquals(action.getControlResultSet().get("C").get(0), "NULL");
        Assert.assertEquals(action.getControlResultSet().get("CNT_F").get(0), "${count}");
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        
        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNotNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/actions/test-sql-query-statements.sql");
        Assert.assertEquals(action.getStatements().size(), 0);
        Assert.assertEquals(action.getControlResultSet().size(), 1);
        Assert.assertEquals(action.getControlResultSet().get("foo").get(0), "1");
        Assert.assertNotNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionManager(), beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class));
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        
        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().get(0), "select A as A_COLUMN, B as B_COLUMN from C");
        Assert.assertEquals(action.getControlResultSet().size(), 2);
        Assert.assertEquals(action.getControlResultSet().get("A_COLUMN").size(), 2);
        Assert.assertEquals(action.getControlResultSet().get("A_COLUMN").get(0), "a");
        Assert.assertEquals(action.getControlResultSet().get("A_COLUMN").get(1), "@ignore@");
        Assert.assertEquals(action.getControlResultSet().get("B_COLUMN").size(), 2);
        Assert.assertEquals(action.getControlResultSet().get("B_COLUMN").get(0), "b");
        Assert.assertEquals(action.getControlResultSet().get("B_COLUMN").get(1), "NULL");
        Assert.assertNull(action.getScriptValidationContext());
        Assert.assertEquals(action.getExtractVariables().size(), 2);
        Assert.assertEquals(action.getExtractVariables().get("A_COLUMN"), "a_values");
        Assert.assertEquals(action.getExtractVariables().get("B_COLUMN"), "b_values");

        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().get(0), "select A as A_COLUMN, B as B_COLUMN from C");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript().trim(), "assert rows.size() == 2");
        
        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 1);
        Assert.assertEquals(action.getStatements().get(0), "select A as A_COLUMN, B as B_COLUMN from C");
        Assert.assertEquals(action.getControlResultSet().size(), 0);
        Assert.assertEquals(action.getExtractVariables().size(), 0);
        
        Assert.assertNotNull(action.getScriptValidationContext());
        Assert.assertNotNull(action.getScriptValidationContext().getValidationScriptResourcePath());
        Assert.assertEquals(action.getScriptValidationContext().getValidationScriptResourcePath(), "classpath:org/citrusframework/script/example.groovy");
        Assert.assertEquals(action.getScriptValidationContext().getValidationScript(), "");
    }
    
    @Test
    public void testMissingDataSourceBeanRef() {
        try {
            createApplicationContext("failed");
            Assert.fail("Missing bean creation exception due to invalid data source");
        } catch (BeanDefinitionStoreException e) {
            Assert.assertTrue(e.getCause().getMessage().contains(
                    "Missing proper data source reference"));
        }
    }
}
