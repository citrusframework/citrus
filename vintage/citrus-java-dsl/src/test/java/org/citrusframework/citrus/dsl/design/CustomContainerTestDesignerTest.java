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

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.container.AbstractActionContainer;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CustomContainerTestDesignerTest extends UnitTestSupport {

    @Test
    public void testCustomContainer() {
        MockTestDesigner builder = new MockTestDesigner(context) {
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                CustomActionContainer container = new CustomActionContainer();
                container.addTestAction(new EchoAction.Builder().message("This is a custom container action").build());
                action(container(container));
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

    private static class CustomActionContainer extends AbstractActionContainer {

        public CustomActionContainer() {
            setName("custom");
        }

        @Override
        public void doExecute(TestContext context) {
            for (int i = 1; i <= 10; i++) {
                for (TestActionBuilder<?> actionBuilder : actions) {
                    TestAction action = actionBuilder.build();
                    action.execute(context);
                }
            }
        }
    }
}
