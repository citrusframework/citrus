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
import com.consol.citrus.container.ConditionExpression;
import com.consol.citrus.container.Conditional;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConditionalTestDesignerTest extends AbstractTestNGUnitTest {
    @Test
    public void testConditionalBuilderNested() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                conditional(echo("${var}")).when("${var} = 5");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");
        
        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getCondition(), "${var} = 5");
    }

    @Test
    public void testConditionalBuilder() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                conditional().when("${var} = 5").actions(echo("${var}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getCondition(), "${var} = 5");
    }

    @Test
    public void testConditionalBuilderConditionExpression() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext) {
            @Override
            public void configure() {
                conditional().when(new ConditionExpression() {
                    @Override
                    public boolean evaluate(TestContext context) {
                        return context.getVariable("var").equals("Hello");
                    }
                }).actions(echo("${var}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertNotNull(container.getConditionExpression());
    }
}
