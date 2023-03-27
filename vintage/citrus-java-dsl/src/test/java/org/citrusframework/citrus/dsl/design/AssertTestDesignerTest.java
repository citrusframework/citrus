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

package org.citrusframework.citrus.dsl.design;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.AbstractTestAction;
import org.citrusframework.citrus.actions.EchoAction;
import org.citrusframework.citrus.container.Assert;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AssertTestDesignerTest extends UnitTestSupport {

    @Test
    public void testAssertBuilder() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Unknown variable 'foo'")
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Assert.class);
        assertEquals(test.getActions().get(0).getName(), "assert");

        Assert container = (Assert)(test.getTestAction(0));

        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getAction().getClass(), EchoAction.class);
        assertEquals(container.getException(), CitrusRuntimeException.class);
        assertEquals(container.getMessage(), "Unknown variable 'foo'");
        assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }

    @Test
    public void testAssertBuilderWithAnonymousAction() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                assertException()
                        .exception(CitrusRuntimeException.class)
                        .message("Unknown variable 'foo'")
                .when(new AbstractTestAction() {
                    @Override
                    public void doExecute(TestContext context) {
                        context.getVariable("foo");
                    }
                });
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Assert.class);
        assertEquals(test.getActions().get(0).getName(), "assert");

        Assert container = (Assert)(test.getTestAction(0));

        assertEquals(container.getActionCount(), 1);
        assertTrue(container.getAction().getClass().isAnonymousClass());
        assertEquals(container.getException(), CitrusRuntimeException.class);
        assertEquals(container.getMessage(), "Unknown variable 'foo'");
    }

}
