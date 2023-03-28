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

package org.citrusframework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.actions.AbstractAsyncTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Async;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.citrusframework.util.TestUtils;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;

public class TestCaseTest extends UnitTestSupport {

    @Test
    public void testExecution() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction.Builder().build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testWaitForFinish() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction.Builder().build());
        testcase.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test(expectedExceptions = TestCaseFailedException.class,
          expectedExceptionsMessageRegExp = "Failed to wait for test container to finish properly")
    public void testWaitForFinishTimeout() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setTimeout(500L);
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction.Builder().build());
        testcase.addTestAction(new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(1000L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testWaitForFinishAsync() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new Async.Builder().actions(() -> new AbstractAsyncTestAction() {
            @Override
            public void doExecuteAsync(final TestContext context) {
                try {
                    Thread.sleep(500L);
                } catch (final InterruptedException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        }).build());

        testcase.execute(context);
        testcase.finish(context);

        // Make sure that waiting thread is completed
        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(TestUtils.WAIT_THREAD_PREFIX.concat("MyTestCase")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> LoggerFactory.getLogger("TestWaitForFinishAsync").warn(Arrays.toString(threads.get(thread))));
        waitingThread.ifPresent(thread -> Assert.fail(String.format("Waiting thread still alive: %s", thread.toString())));
    }

    @Test
    public void testExecutionWithVariables() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        final Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("name", "Citrus");
        variables.put("framework", "${name}");
        variables.put("hello", "citrus:concat('Hello ', ${name}, '!')");
        variables.put("goodbye", "Goodbye ${name}!");
        variables.put("welcome", "Welcome ${name}, today is citrus:currentDate()!");
        testcase.setVariableDefinitions(variables);

        testcase.addTestAction(action(context -> {
            Assert.assertEquals(context.getVariables().get(CitrusSettings.TEST_NAME_VARIABLE), "MyTestCase");
            Assert.assertEquals(context.getVariables().get(CitrusSettings.TEST_PACKAGE_VARIABLE), TestCase.class.getPackage().getName());
            Assert.assertEquals(context.getVariable("${name}"), "Citrus");
            Assert.assertEquals(context.getVariable("${framework}"), "Citrus");
            Assert.assertEquals(context.getVariable("${hello}"), "Hello Citrus!");
            Assert.assertEquals(context.getVariable("${goodbye}"), "Goodbye Citrus!");
            Assert.assertEquals(context.getVariable("${welcome}"), "Welcome Citrus, today is " + new CurrentDateFunction().execute(new ArrayList<>(), context) + "!");
        }));

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testUnknownVariable() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));

        testcase.addTestAction(action(context -> Assert.assertEquals(context.getVariable("${unknown}"), message)));

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class}, expectedExceptionsMessageRegExp = "This failed in forked action")
    public void testExceptionInContext() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(action(context -> {
                context.addException(new CitrusRuntimeException("This failed in forked action"));
        }).build());

        testcase.addTestAction(new EchoAction.Builder().message("Everything is fine!").build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test(expectedExceptions = {TestCaseFailedException.class})
    public void testExceptionInContextInFinish() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testFinalActions() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.addTestAction(new EchoAction.Builder().build());
        testcase.addFinalAction(new EchoAction.Builder().build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testThreadLeak() {

        //GIVEN
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("ThreadLeakTest");
        testcase.addTestAction(new EchoAction.Builder().build());

        //WHEN
        testcase.execute(context);
        testcase.finish(context);

        //THEN
        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(TestUtils.WAIT_THREAD_PREFIX.concat("ThreadLeakTest")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> Assert.fail(String.format("Waiting thread still alive: %s", thread.toString())));
    }

}
