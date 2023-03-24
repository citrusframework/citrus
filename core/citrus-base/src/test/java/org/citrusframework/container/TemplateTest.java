/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.container;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


/**
 * @author Christoph Deppisch
 */
public class TemplateTest extends UnitTestSupport {

    private final TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testTemplateExecution() {
        reset(action);

        Template template = new Template.Builder()
                .actions(action)
                .build();
        template.execute(context);

        verify(action).execute(context);
    }

    @Test
    public void testTemplateWithParams() {
        context.setVariable("text", "Hello Citrus!");

        EchoAction echo = new EchoAction.Builder().message("${myText}").build();

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param", "Parameter was set");
        parameters.put("myText", "${text}");

        Assert.assertFalse(context.getVariables().containsKey("param"));
        Assert.assertFalse(context.getVariables().containsKey("myText"));

        Template template = new Template.Builder()
                .actions(echo)
                .parameters(parameters)
                .build();
        template.execute(context);

        Assert.assertTrue(context.getVariables().containsKey("param"),
        "Missing new variable 'param' in global test context");
        Assert.assertEquals(context.getVariable("param"), "Parameter was set");

        Assert.assertTrue(context.getVariables().containsKey("myText"),
                "Missing new variable 'myText' in global test context");
        Assert.assertEquals(context.getVariable("myText"), "Hello Citrus!");
    }

    @Test
    public void testTemplateWithParamsLocalContext() {
        context.setVariable("text", "Hello Citrus!");

        EchoAction echo = new EchoAction.Builder().message("${myText}").build();

        Assert.assertFalse(context.getVariables().containsKey("myText"));

        Template template = new Template.Builder()
                .actions(echo)
                .parameter("myText", "${text}")
                .globalContext(false)
                .build();
        template.execute(context);

        Assert.assertFalse(context.getVariables().containsKey("myText"),
                "Variable 'myText' present in global test context, although global context was disabled before");
    }

    @Test
    public void testTemplateMissingParams() {
        context.setVariable("text", "Hello Citrus!");

        EchoAction echo = new EchoAction.Builder().message("${myText}").build();

        Template template = new Template.Builder()
                .actions(echo)
                .build();
        try {
            template.execute(context);
        } catch (CitrusRuntimeException e) {
            return;
        }

        Assert.fail("Missing CitrusRuntimeException due to unknown parameter");
    }
}
