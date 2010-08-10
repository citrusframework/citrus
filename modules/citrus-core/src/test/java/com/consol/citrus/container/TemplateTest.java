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

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.Template;
import com.consol.citrus.testng.AbstractBaseTest;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class TemplateTest extends AbstractBaseTest {
    
    @Test
    public void testTemplate() {
        Template template = new Template();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        actions.add(action);
        template.setActions(actions);
        
        template.equals(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testParams() {
        Template template = new Template();
        
        context.setVariable("text", "Hello Citrus!");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        EchoAction echo = new EchoAction();
        echo.setMessage("${myText}");
        
        actions.add(echo);
        template.setActions(actions);
        
        template.setParameter(Collections.singletonMap("myText", "${text}"));
        template.equals(context);
    }
}
