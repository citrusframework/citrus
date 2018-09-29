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

import com.consol.citrus.actions.*;
import com.consol.citrus.container.Async;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.functions.core.CurrentDateFunction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class TestCaseTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testExecution() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        testcase.addTestAction(new EchoAction());
        
        testcase.execute(context);
    }

    @Test
    public void testWaitForFinish() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction());
        testcase.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        testcase.execute(context);
    }

    @Test(expectedExceptions = TestCaseFailedException.class, expectedExceptionsMessageRegExp = "Failed to wait for nested test actions to finish properly")
    public void testWaitForFinishTimeout() {
        TestCase testcase = new TestCase();
        testcase.setTimeout(500L);
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction());
        testcase.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(TestContext context) {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        testcase.execute(context);
    }

    @Test
    public void testWaitForFinishAsync() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new Async().addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        }));

        testcase.execute(context);
    }
    
    @Test
    public void testExecutionWithVariables() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        Map<String, Object> variables = new LinkedHashMap<String, Object>();
        variables.put("name", "Citrus");
        variables.put("framework", "${name}");
        variables.put("hello", "citrus:concat('Hello ', ${name}, '!')");
        variables.put("goodbye", "Goodbye ${name}!");
        variables.put("welcome", "Welcome ${name}, today is citrus:currentDate()!");
        testcase.setVariableDefinitions(variables);
        
        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariables().get(Citrus.TEST_NAME_VARIABLE), "MyTestCase");
                Assert.assertEquals(context.getVariables().get(Citrus.TEST_PACKAGE_VARIABLE), TestCase.class.getPackage().getName());
                Assert.assertEquals(context.getVariable("${name}"), "Citrus");
                Assert.assertEquals(context.getVariable("${framework}"), "Citrus");
                Assert.assertEquals(context.getVariable("${hello}"), "Hello Citrus!");
                Assert.assertEquals(context.getVariable("${goodbye}"), "Goodbye Citrus!");
                Assert.assertEquals(context.getVariable("${welcome}"), "Welcome Citrus, today is " + new CurrentDateFunction().execute(new ArrayList<String>(), context) + "!");
            }
        });
        
        testcase.execute(context);
    }
    
    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testUnknownVariable() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.<String, Object>singletonMap("text", message));
        
        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Assert.assertEquals(context.getVariable("${unknown}"), message);
            }
        });
        
        testcase.execute(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class}, expectedExceptionsMessageRegExp = "This failed in forked action")
    public void testExceptionInContext() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                context.addException(new CitrusRuntimeException("This failed in forked action"));
            }
        });

        testcase.addTestAction(new EchoAction().setMessage("Everything is fine!"));

        testcase.execute(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testExceptionInContextInFinish() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                context.addException(new CitrusRuntimeException("This failed in forked action"));
            }
        });

        testcase.execute(context);
    }
    
    @Test
    public void testFinalActions() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        testcase.addTestAction(new EchoAction());
        testcase.addFinalAction(new EchoAction());
        
        testcase.execute(context);
    }

    @Test
    public void testThreadLeak() {
        TestCase testcase = new TestCase();
        testcase.setName("ThreadLeakTestCase");
        testcase.addTestAction(new EchoAction());
        testcase.execute(context);

        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Assert.assertEquals(threadSet.stream().filter(t -> t.getName().startsWith(TestCase.FINISHER_THREAD_PREFIX)).filter(t -> t.isAlive()).count(), 0);
    }

}
