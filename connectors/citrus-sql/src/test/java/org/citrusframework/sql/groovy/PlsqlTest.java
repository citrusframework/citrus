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
import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.spi.BindToRegistry;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PlsqlTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    private final BasicDataSource dataSource = new BasicDataSource();

    @BindToRegistry
    private final PlatformTransactionManager mockTransactionManager = Mockito.mock(PlatformTransactionManager.class);

    @BeforeClass
    public void setupDataSource() {
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:plsql-groovy-test");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
    }

    @Test
    public void shouldLoadPlsql() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/sql/groovy/plsql.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "PlsqlTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ExecutePLSQLAction.class);

        int actionIndex = 0;

        ExecutePLSQLAction action = (ExecutePLSQLAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertNotNull(action.getDataSource());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNotNull(action.getSqlResourcePath());
        Assert.assertNull(action.getScript());
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");

        action = (ExecutePLSQLAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getName(), "plsql");
        Assert.assertNotNull(action.getDataSource());
        Assert.assertEquals(action.getDataSource(), dataSource);
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertTrue(action.getScript().length() > 0);
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getTransactionManager(), mockTransactionManager);
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
    }
}
