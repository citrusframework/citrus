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

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.CreateVariablesAction;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;

public class CreateVariablesTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testCreateVariablesBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(createVariable("foo", "bar"));
        builder.$(createVariable("text", "Hello Citrus!"));
        builder.$(createVariable("foobar", "bars"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 3);
        Assert.assertEquals(test.getActions().get(0).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), CreateVariablesAction.class);
        Assert.assertEquals(test.getActions().get(2).getClass(), CreateVariablesAction.class);

        CreateVariablesAction action = (CreateVariablesAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{foo=bar}");

        action = (CreateVariablesAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{text=Hello Citrus!}");

        action = (CreateVariablesAction)test.getActions().get(2);
        Assert.assertEquals(action.getName(), "create-variables");
        Assert.assertEquals(action.getVariables().toString(), "{foobar=bars}");
    }
}
