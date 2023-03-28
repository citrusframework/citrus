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

package org.citrusframework.dsl.design;

import org.citrusframework.TestCase;
import org.citrusframework.actions.*;
import org.citrusframework.container.Sequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Christoph Deppisch
 * @since 1.3
 */
public class SequenceTestDesignerTest extends UnitTestSupport {

    @Test
    public void testSequenceBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                sequential().actions(echo("${var}"), sleep(5000L));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        assertEquals(test.getActions().get(0).getName(), "sequential");

        Sequence container = (Sequence)test.getActions().get(0);
        assertEquals(container.getActionCount(), 2);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
    }

    @Test
    public void testSequenceBuilderWithAnonymousAction() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                sequential().actions(
                        echo("${var}"),
                        () -> new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                context.setVariable("anonymous", "anonymous");
                            }
                        },
                        sleep(5000L),
                        () -> new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                context.getVariable("anonymous");
                            }
                        });
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Sequence.class);
        assertEquals(test.getActions().get(0).getName(), "sequential");

        Sequence container = (Sequence)test.getActions().get(0);
        assertEquals(container.getActionCount(), 4);
        assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        assertTrue(container.getActions().get(1).getClass().isAnonymousClass());
        assertEquals(container.getActions().get(2).getClass(), SleepAction.class);
        assertTrue(container.getActions().get(3).getClass().isAnonymousClass());
    }
}
