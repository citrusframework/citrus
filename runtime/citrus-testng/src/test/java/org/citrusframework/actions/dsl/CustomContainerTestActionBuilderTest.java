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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.context.TestContext;
import org.testng.annotations.Test;

import static org.citrusframework.AbstractTestContainerBuilder.container;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class CustomContainerTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testCustomContainer() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(container(new CustomActionContainer()).actions(
                echo("Hello"),
                echo("${index}")
        ));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), CustomActionContainer.class);
        assertEquals(test.getActions().get(0).getName(), "custom");

        CustomActionContainer container = (CustomActionContainer) test.getActions().get(0);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(0)).getMessage(), "Hello");
        assertEquals(container.getActions().get(1).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(1)).getMessage(), "${index}");

        assertEquals(context.getVariable("index"), "10");
    }

    @Test
    public void testCustomContainerWithPredefinedActions() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        CustomActionContainer container = new CustomActionContainer();
        container.addTestAction(new EchoAction.Builder().message("This is a custom container action").build());
        builder.$(container(container));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), CustomActionContainer.class);
        assertEquals(test.getActions().get(0).getName(), "custom");

        container = (CustomActionContainer) test.getActions().get(0);
        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertEquals(((EchoAction)container.getActions().get(0)).getMessage(), "This is a custom container action");

        assertEquals(context.getVariable("index"), "10");
    }

    private static class CustomActionContainer extends AbstractActionContainer {

        public CustomActionContainer() {
            setName("custom");
        }

        @Override
        public void doExecute(TestContext context) {
            for (int i = 1; i <= 10; i++) {
                context.setVariable("index", i);

                for (TestActionBuilder<?> actionBuilder : actions) {
                    executeAction(actionBuilder.build(), context);
                }
            }
        }
    }
}
