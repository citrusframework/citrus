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
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class TraceVariablesTest extends AbstractGroovyActionDslTest {

    @Test
    public void shouldLoadTraceVariables() {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/trace-variables.test.groovy");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "TraceVariablesTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), TraceVariablesAction.class);

        int actionIndex = 0;

        TraceVariablesAction action = (TraceVariablesAction) result.getTestAction(actionIndex++);

        Assert.assertEquals(action.getVariableNames().size(), 0L);

        action = (TraceVariablesAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getVariableNames().size(), 1L);
        Assert.assertEquals(action.getVariableNames().get(0), "foo");

        action = (TraceVariablesAction) result.getTestAction(actionIndex);
        Assert.assertEquals(action.getVariableNames().size(), 2L);
        Assert.assertEquals(action.getVariableNames().get(0), "foo");
        Assert.assertEquals(action.getVariableNames().get(1), "bar");
    }

}
