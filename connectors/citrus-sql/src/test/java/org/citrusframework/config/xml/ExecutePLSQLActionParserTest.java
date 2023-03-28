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

import org.citrusframework.actions.ExecutePLSQLAction;
import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.citrusframework.testng.AbstractActionParserTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ExecutePLSQLActionParserTest extends AbstractActionParserTest<ExecutePLSQLAction> {

    @Test
    public void testPLSQLActionParser() {
        assertActionCount(2);

        ExecutePLSQLAction action = getNextTestActionFromTest();
        Assert.assertEquals(action.getName(), "plsql:testDataSource");
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNotNull(action.getSqlResourcePath());
        Assert.assertNull(action.getScript());
        Assert.assertFalse(action.isIgnoreErrors());
        Assert.assertNull(action.getTransactionManager());
        Assert.assertEquals(action.getTransactionTimeout(), "-1");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_DEFAULT");

        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getName(), "plsql:testDataSource");
        Assert.assertNotNull(action.getDataSource());
        Assert.assertNull(action.getSqlResourcePath());
        Assert.assertTrue(action.getScript().length() > 0);
        Assert.assertTrue(action.isIgnoreErrors());
        Assert.assertEquals(action.getTransactionManager(), beanDefinitionContext.getBean("testTransactionManager", PlatformTransactionManager.class));
        Assert.assertEquals(action.getTransactionTimeout(), "5000");
        Assert.assertEquals(action.getTransactionIsolationLevel(), "ISOLATION_READ_COMMITTED");
    }

    @Test
    public void shouldLookupTestActionParser() {
        Assert.assertTrue(CitrusNamespaceParserRegistry.lookupBeanParser().containsKey("plsql"));
        Assert.assertEquals(CitrusNamespaceParserRegistry.lookupBeanParser().get("plsql").getClass(), ExecutePLSQLActionParser.class);

        Assert.assertEquals(CitrusNamespaceParserRegistry.getBeanParser("plsql").getClass(), ExecutePLSQLActionParser.class);
    }
}
