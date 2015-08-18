/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.CreateVariablesAction;

public class CreateVariablesTestDesignerTest extends AbstractTestNGUnitTest {

    @Test
    public void testCreateVariablesBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                createVariable("foo", "bar");
                createVariable("text", "Hello Citrus!");

                createVariable("foobar", "bars");
            }
        };

        builder.configure();

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
