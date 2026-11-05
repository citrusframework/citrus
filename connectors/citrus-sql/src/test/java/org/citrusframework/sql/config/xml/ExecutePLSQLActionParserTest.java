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

import org.citrusframework.sql.actions.ExecutePLSQLAction;
import org.citrusframework.spring.config.CitrusNamespaceParserRegistry;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExecutePLSQLActionParserTest extends AbstractActionParserTest<ExecutePLSQLAction> {

    @Test
    public void testPLSQLActionParser() {
        assertActionCount(2);

        ExecutePLSQLAction action = getNextTestActionFromTest();
        Assertions.assertEquals("plsql:testDataSource", action.getName());
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNotNull(action.getSqlResourcePath());
        Assertions.assertNull(action.getScript());
        Assertions.assertFalse(action.isIgnoreErrors());
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());

        action = getNextTestActionFromTest();
        Assertions.assertEquals("plsql:testDataSource", action.getName());
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertTrue(!action.getScript().isEmpty());
        Assertions.assertTrue(action.isIgnoreErrors());
        Assertions.assertEquals(beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class), action.getTransactionManager());
        Assertions.assertEquals("5000", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_READ_COMMITTED", action.getTransactionIsolationLevel());
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assertions.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("plsql"));
        Assertions.assertEquals(ExecutePLSQLActionParser.class, CitrusNamespaceParserRegistry.lookupBeanParser().get("plsql").getClass());

        Assertions.assertEquals(ExecutePLSQLActionParser.class, CitrusNamespaceParserRegistry.getBeanParser("plsql").getClass());
    }
}
