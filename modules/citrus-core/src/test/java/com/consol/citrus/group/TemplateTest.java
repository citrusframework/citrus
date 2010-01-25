/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.group;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.EchoAction;
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
