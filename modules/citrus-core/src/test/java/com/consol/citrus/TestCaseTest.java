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

package com.consol.citrus;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class TestCaseTest extends AbstractBaseTest {
    
    @Test
    public void testExecution() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoAction());
        
        testcase.execute();
    }
    
    @Test
    public void testExecutionWithVariables() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${text}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testUnknownVariable() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${unknown}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test
    public void testFinallyChain() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoAction());
        testcase.addFinallyChainAction(new EchoAction());
        
        testcase.execute();
        testcase.finish();
    }
}
