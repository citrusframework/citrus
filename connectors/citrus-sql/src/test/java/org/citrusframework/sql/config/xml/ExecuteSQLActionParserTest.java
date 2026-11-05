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

import org.citrusframework.sql.actions.ExecuteSQLAction;
import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExecuteSQLActionParserTest extends AbstractActionParserTest<ExecuteSQLAction> {

    @Test
    public void testSQLActionParser() {
        assertActionCount(2);
        assertActionClassAndName(ExecuteSQLAction.class, "sqlUpdate:testDataSource");

        // 1st action
        ExecuteSQLAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertEquals(2, action.getStatements().size());
        Assertions.assertEquals("insert into foo_table values (foo, foo)", action.getStatements().get(0));
        Assertions.assertEquals("update foo_table set foo=foo where foo=foo", action.getStatements().get(1));
        Assertions.assertFalse(action.isIgnoreErrors());
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());

        // 2nd action
        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNotNull(action.getSqlResourcePath());
        Assertions.assertEquals("classpath:org/citrusframework/sql/actions/test-sql-statements.sql", action.getSqlResourcePath());
        Assertions.assertEquals(0, action.getStatements().size());
        Assertions.assertTrue(action.isIgnoreErrors());
        Assertions.assertEquals(beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class), action.getTransactionManager());
        Assertions.assertEquals("5000", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_READ_COMMITTED", action.getTransactionIsolationLevel());
    }

    @Test
    public void testMissingDataSourceBeanRef() {
        BeanDefinitionStoreException e = Assertions.assertThrows(BeanDefinitionStoreException.class, () -> createApplicationContext("failed"));
        Assertions.assertTrue(e.getCause().getMessage().contains(
                "Missing proper data source reference"));
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assertions.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("sql"));
        Assertions.assertEquals(SQLActionParser.class, CitrusNamespaceParserRegistry.lookupBeanParser().get("sql").getClass());

        Assertions.assertEquals(SQLActionParser.class, CitrusNamespaceParserRegistry.getBeanParser("sql").getClass());
    }
}
