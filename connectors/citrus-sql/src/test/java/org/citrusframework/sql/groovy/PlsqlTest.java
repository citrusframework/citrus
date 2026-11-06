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

package org.citrusframework.sql.groovy;

import org.apache.commons.dbcp2.BasicDataSource;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.sql.actions.ExecutePLSQLAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.spi.BindToRegistry;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlsqlTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    private final BasicDataSource dataSource = new BasicDataSource();

    @BindToRegistry
    private final PlatformTransactionManager mockTransactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BeforeAll
    public void setupDataSource() {
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:plsql-groovy-test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
    }

    @Test
    public void shouldLoadPlsql() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/groovy/plsql.citrus.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("PlsqlTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(ExecutePLSQLAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        ExecutePLSQLAction action = (ExecutePLSQLAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("plsql", action.getName());
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertEquals(dataSource, action.getDataSource());
        Assertions.assertNotNull(action.getSqlResourcePath());
        Assertions.assertNull(action.getScript());
        Assertions.assertTrue(action.isIgnoreErrors());
        Assertions.assertNull(action.getTransactionManager());
        Assertions.assertEquals("-1", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_DEFAULT", action.getTransactionIsolationLevel());

        action = (ExecutePLSQLAction) result.getTestAction(actionIndex);
        Assertions.assertEquals("plsql", action.getName());
        Assertions.assertNotNull(action.getDataSource());
        Assertions.assertEquals(dataSource, action.getDataSource());
        Assertions.assertNull(action.getSqlResourcePath());
        Assertions.assertTrue(!action.getScript().isEmpty());
        Assertions.assertTrue(action.isIgnoreErrors());
        Assertions.assertEquals(mockTransactionManager, action.getTransactionManager());
        Assertions.assertEquals("5000", action.getTransactionTimeout());
        Assertions.assertEquals("ISOLATION_READ_COMMITTED", action.getTransactionIsolationLevel());
    }
}
