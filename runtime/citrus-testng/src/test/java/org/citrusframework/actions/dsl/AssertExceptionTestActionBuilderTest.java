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
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.container.Assert;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.FailAction.Builder.fail;
import static org.citrusframework.container.Assert.Builder.assertException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AssertExceptionTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testAssertDefaultExceptionBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(assertException().when(fail("Error!")));

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), Assert.class);
        assertEquals(test.getActions().get(0).getName(), "assert");

        Assert container = (Assert)(test.getTestAction(0));

        assertEquals(container.getActionCount(), 1);
        assertEquals(container.getAction().getClass(), FailAction.class);
        assertEquals(container.getException(), CitrusRuntimeException.class);
    }

    @Test
    public void testAssertBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(assertException().exception(CitrusRuntimeException.class)
                    .message("Unknown variable 'foo'")
            .when(echo("${foo}")));

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
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(assertException().exception(CitrusRuntimeException.class)
                    .message("Unknown variable 'foo'")
            .when(new AbstractTestAction() {
                @Override
                public void doExecute(TestContext context) {
                    context.getVariable("foo");
                }
            }));

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
