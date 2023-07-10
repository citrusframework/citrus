/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.groovy.dsl;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class CreateVariablesTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadCreateVariables() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/create-variables.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateVariablesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActions().get(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(result.getActions().get(1).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(result.getActions().get(2).getClass(), CreateVariablesAction.class);

        CreateVariablesAction action = (CreateVariablesAction)result.getActions().get(0);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{foo=bar}");

        action = (CreateVariablesAction)result.getActions().get(1);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{text=Hello Citrus!}");

        action = (CreateVariablesAction)result.getActions().get(2);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{foobar=bars}");
    }
}
