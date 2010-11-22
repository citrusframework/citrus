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
        testcase.setName("MyTestCase");
        
        testcase.addTestAction(new EchoAction());
        
        testcase.execute(context);
    }
    
    @Test
    public void testExecutionWithVariables() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${text}"), message);
            }
        });
        
        testcase.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testUnknownVariable() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${unknown}"), message);
            }
        });
        
        testcase.execute(context);
    }
    
    @Test
    public void testFinallyChain() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        testcase.addTestAction(new EchoAction());
        testcase.addFinallyChainAction(new EchoAction());
        
        testcase.execute(context);
        testcase.finish();
    }
}
