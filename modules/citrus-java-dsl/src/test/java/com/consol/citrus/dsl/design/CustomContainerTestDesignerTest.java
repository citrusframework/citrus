/*
 * Copyright 2006-2016 the original author or authors.
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

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CustomContainerTestDesignerTest extends AbstractTestNGUnitTest {

    @Test
    public void testCustomContainer() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                container(new CustomActionContainer()).actions(
                        echo("Hello"),
                        echo("Citrus")
                );
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), CustomActionContainer.class);
        assertEquals(test.getActions().get(0).getName(), "custom");

        CustomActionContainer container = (CustomActionContainer) test.getActions().get(0);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(0)).getMessage(), "Hello");
        assertEquals(container.getActions().get(1).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(1)).getMessage(), "Citrus");
    }

    @Test
    public void testCustomContainerWithPredefinedActions() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                CustomActionContainer container = new CustomActionContainer();
                container.getActions().add(new EchoAction().setMessage("This is a custom container action"));
                action(container(container).build());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), CustomActionContainer.class);
        assertEquals(test.getActions().get(0).getName(), "custom");

        CustomActionContainer container = (CustomActionContainer) test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(0)).getMessage(), "This is a custom container action");
    }

    private class CustomActionContainer extends AbstractActionContainer {

        public CustomActionContainer() {
            setName("custom");
        }

        @Override
        public void doExecute(TestContext context) {
            for (int i = 1; i <= 10; i++) {
                for (TestAction action : actions) {
                    action.execute(context);
                }
            }
        }
    }
}
