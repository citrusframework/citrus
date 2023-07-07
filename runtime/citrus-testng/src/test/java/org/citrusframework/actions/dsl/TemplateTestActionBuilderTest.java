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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.TraceVariablesAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.container.Template;
import org.citrusframework.context.TestContext;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.Template.Builder.applyTemplate;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class TemplateTestActionBuilderTest extends UnitTestSupport {

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    @Test
    public void testTemplateBuilder() {
        List<TestAction> actions = new ArrayList<>();
        actions.add(new EchoAction.Builder().build());
        actions.add(new TraceVariablesAction.Builder().build());
        Template rootTemplate = new Template.Builder()
                .templateName("fooTemplate")
                .actions(actions)
                .build();

        reset(referenceResolver);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("fooTemplate", Template.class)).thenReturn(rootTemplate);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(applyTemplate("fooTemplate")
                        .parameter("param", "foo")
                        .parameter("text", "Citrus rocks!"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Template.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "template:fooTemplate");

        Template container = (Template)test.getActions().get(0);
        Assert.assertTrue(container.isGlobalContext());
        Assert.assertEquals(container.getParameter().toString(), "{param=foo, text=Citrus rocks!}");
        Assert.assertEquals(container.getActions().size(), 2);
        Assert.assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
        Assert.assertEquals(container.getActions().get(1).getClass(), TraceVariablesAction.class);
    }

    @Test
    public void testTemplateBuilderGlobalContext() {
        Template rootTemplate = new Template.Builder()
                .templateName("fooTemplate")
                .actions(new EchoAction.Builder().build())
                .build();

        reset(referenceResolver);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("fooTemplate", Template.class)).thenReturn(rootTemplate);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(applyTemplate("fooTemplate")
                        .globalContext(false));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActions().size(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Template.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "template:fooTemplate");

        Template container = (Template)test.getActions().get(0);
        Assert.assertFalse(container.isGlobalContext());
        Assert.assertEquals(container.getParameter().size(), 0L);
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getActions().get(0).getClass(), EchoAction.class);
    }
}
