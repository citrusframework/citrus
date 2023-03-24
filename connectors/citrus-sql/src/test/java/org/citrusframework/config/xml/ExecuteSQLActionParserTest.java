/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.config.xml;

import org.citrusframework.actions.ExecuteSQLAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ExecuteSQLActionParserTest extends AbstractActionParserTest<ExecuteSQLAction> {

    @Test
    public void testSQLActionParser() {
        assertActionCount(2);
        assertActionClassAndName(ExecuteSQLAction.class, "sqlUpdate:testDataSource");

        // 1st action
        ExecuteSQLAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getStatements().size(), 2);
        Assert.assertEquals(action.getStatements().get(0), "insert into foo_table values (foo, foo)");
        Assert.assertEquals(action.getStatements().get(1), "update foo_table set foo=foo where foo=foo");
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNotNull(action.getSqlResourcePath());
        Assert.assertEquals(action.getSqlResourcePath(), "classpath:org/citrusframework/actions/test-sql-statements.sql");
        Assert.assertEquals(action.getStatements().size(), 0);
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getTransactionManager(), beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class));
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
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

    @Test
    public void shouldLookupTestActionParser() {
        Assert.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("sql"));
        Assert.assertEquals(CitrusNamespaceParserRegistry.lookupBeanParser().get("sql").getClass(), SQLActionParser.class);

        Assert.assertEquals(CitrusNamespaceParserRegistry.getBeanParser("sql").getClass(), SQLActionParser.class);
    }
}
