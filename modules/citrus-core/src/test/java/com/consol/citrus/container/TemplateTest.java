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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class TemplateTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testTemplateExecution() {
        Template template = new Template();

        reset(action);

        template.setActions(Collections.singletonList(action));

        template.execute(context);

        verify(action).execute(context);
    }
    
    @Test
    public void testTemplateWithParams() {
        Template template = new Template();
        
        context.setVariable("text", "Hello Citrus!");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        EchoAction echo = new EchoAction();
        echo.setMessage("${myText}");
        
        actions.add(echo);
        template.setActions(actions);
        
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("param", "Parameter was set");
        parameters.put("myText", "${text}");
        
        template.setParameter(parameters);
        
        Assert.assertFalse(context.getVariables().containsKey("param"));
        Assert.assertFalse(context.getVariables().containsKey("myText"));
        
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
        Template template = new Template();
        
        context.setVariable("text", "Hello Citrus!");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        EchoAction echo = new EchoAction();
        echo.setMessage("${myText}");
        
        actions.add(echo);
        template.setActions(actions);
        
        template.setParameter(Collections.singletonMap("myText", "${text}"));
        template.setGlobalContext(false);
        
        Assert.assertFalse(context.getVariables().containsKey("myText"));
        
        template.execute(context);
        
        Assert.assertFalse(context.getVariables().containsKey("myText"), 
                "Variable 'myText' present in global test context, although global context was disabled before");
    }
    
    @Test
    public void testTemplateMissingParams() {
        Template template = new Template();
        
        context.setVariable("text", "Hello Citrus!");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        EchoAction echo = new EchoAction();
        echo.setMessage("${myText}");
        
        actions.add(echo);
        template.setActions(actions);
        
        try {
            template.execute(context);
        } catch (CitrusRuntimeException e) {
            return;
        }
        
        Assert.fail("Missing CitrusRuntimeException due to unknown parameter");
    }
}
