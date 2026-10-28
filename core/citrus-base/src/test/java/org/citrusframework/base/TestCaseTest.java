/*
 * Copyright the original author or authors.
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

package org.citrusframework.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.CitrusSettings;
import org.citrusframework.TestCase;
import org.citrusframework.actions.AbstractAsyncTestAction;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.Async;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.base.functions.core.CurrentDateFunction;
import org.citrusframework.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestCaseTest extends UnitTestSupport implements TestActionSupport {

    @Test
    public void testExecution() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        testcase.addTestAction(new EchoAction.Builder().build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testWaitForFinish() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

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

    @Test
    public void testWaitForFinishTimeout() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setTimeout(500L);
        testcase.setName("TestCaseTest");

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
        TestCaseFailedException thrown = assertThrows(TestCaseFailedException.class, () -> testcase.finish(context));
        assertThat(thrown).hasMessage("Failed to wait for test container in test 'TestCaseTest' to finish properly");
    }

    @Test
    public void testWaitForFinishAsync() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

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
                .filter(t -> t.getName().startsWith(TestUtils.WAIT_THREAD_PREFIX.concat("TestCaseTest")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> LoggerFactory.getLogger("TestWaitForFinishAsync").warn(Arrays.toString(threads.get(thread))));
        waitingThread.ifPresent(thread -> Assertions.fail(String.format("Waiting thread still alive: %s", thread)));
    }

    @Test
    public void testExecutionWithVariables() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        final Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("name", "Citrus");
        variables.put("framework", "${name}");
        variables.put("hello", "citrus:concat('Hello ', ${name}, '!')");
        variables.put("goodbye", "Goodbye ${name}!");
        variables.put("welcome", "Welcome ${name}, today is citrus:currentDate()!");
        testcase.setVariableDefinitions(variables);

        testcase.addTestAction(action(context -> {
            Assertions.assertEquals("TestCaseTest", context.getVariables().get(CitrusSettings.getTestNameVariable()));
            Assertions.assertEquals(DefaultTestCase.class.getPackage().getName(), context.getVariables().get(CitrusSettings.getTestPackageVariable()));
            Assertions.assertEquals("Citrus", context.getVariable("${name}"));
            Assertions.assertEquals("Citrus", context.getVariable("${framework}"));
            Assertions.assertEquals("Hello Citrus!", context.getVariable("${hello}"));
            Assertions.assertEquals("Goodbye Citrus!", context.getVariable("${goodbye}"));
            Assertions.assertEquals("Welcome Citrus, today is " + new CurrentDateFunction().execute(new ArrayList<>(), context) + "!", context.getVariable("${welcome}"));
        }));

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testUnknownVariable() {
        final DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));

        testcase.addTestAction(action(context -> Assertions.assertEquals(message, context.getVariable("${unknown}"))));

        assertThrows(TestCaseFailedException.class, () -> {
            testcase.execute(context);
            testcase.finish(context);
        });
    }

    @Test
    public void testExceptionInContext() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        testcase.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        testcase.addTestAction(new EchoAction.Builder().message("Everything is fine!").build());

        TestCaseFailedException thrown = assertThrows(TestCaseFailedException.class, () -> {
            testcase.execute(context);
            testcase.finish(context);
        });
        assertThat(thrown).hasMessage("This failed in forked action");
    }

    @Test
    public void testExceptionInContextInFinish() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        testcase.addTestAction(action(context -> context.addException(new CitrusRuntimeException("This failed in forked action"))).build());

        testcase.execute(context);
        assertThrows(TestCaseFailedException.class, () -> testcase.finish(context));
    }

    @Test
    public void testFinalActions() {
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("TestCaseTest");

        testcase.addTestAction(new EchoAction.Builder().build());
        testcase.addFinalAction(new EchoAction.Builder().build());

        testcase.execute(context);
        testcase.finish(context);
    }

    @Test
    public void testThreadLeak() {

        //GIVEN
        final TestCase testcase = new DefaultTestCase();
        testcase.setName("ThreadLeakTestCaseTest");
        testcase.addTestAction(new EchoAction.Builder().build());

        //WHEN
        testcase.execute(context);
        testcase.finish(context);

        //THEN
        final Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        Optional<Thread> waitingThread = threads.keySet().stream()
                .filter(t -> t.getName().startsWith(TestUtils.WAIT_THREAD_PREFIX.concat("ThreadLeakTestCaseTest")))
                .filter(Thread::isAlive)
                .findAny();

        waitingThread.ifPresent(thread -> Assertions.fail(String.format("Waiting thread still alive: %s", thread)));
    }
}
