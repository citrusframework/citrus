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

package org.citrusframework.actions;

import java.util.Map;

import org.citrusframework.TestActionBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TestActionBuilderLookupTest {

    @Test
    public void shouldLookupTestActions() {
        Map<String, TestActionBuilder<?>> endpointBuilders = TestActionBuilder.lookup();
        Assert.assertTrue(endpointBuilders.containsKey("sql"));
        Assert.assertTrue(endpointBuilders.containsKey("query"));
        Assert.assertTrue(endpointBuilders.containsKey("plsql"));
    }

    @Test
    public void shouldLookupTestActionByName() {
        Assert.assertTrue(TestActionBuilder.lookup("sql").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("sql").get().getClass(), ExecuteSQLAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("query").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("query").get().getClass(), ExecuteSQLQueryAction.Builder.class);
        Assert.assertTrue(TestActionBuilder.lookup("plsql").isPresent());
        Assert.assertEquals(TestActionBuilder.lookup("plsql").get().getClass(), ExecutePLSQLAction.Builder.class);
    }
}
