/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.actions.dsl;

import java.util.Collections;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.TraceVariablesAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.TraceVariablesAction.Builder.traceVariables;

public class TraceVariablesTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testTraceVariablesBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("variable1", "foo");
        builder.variable("variable2", "bar");

        builder.variable("ONE", "1");
        builder.variable("TWO", "2");

        builder.variable("TWELFE", "${ONE}${TWO}");

        builder.$(traceVariables());
        builder.$(traceVariables("variable1", "variable2"));
        builder.$(traceVariables("ONE", "TWO", "TWELFE"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), TraceVariablesAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), TraceVariablesAction.class);
        Assert.assertEquals(test.getActions().get(2).getClass(), TraceVariablesAction.class);

        TraceVariablesAction action = (TraceVariablesAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "trace");
        Assert.assertEquals(action.getVariableNames(), Collections.emptyList());

        action = (TraceVariablesAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "trace");
        Assert.assertNotNull(action.getVariableNames());
        Assert.assertEquals(action.getVariableNames().toString(), "[variable1, variable2]");
    }
}
