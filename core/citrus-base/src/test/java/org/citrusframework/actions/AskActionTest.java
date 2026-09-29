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

package org.citrusframework.actions;

import java.util.concurrent.atomic.AtomicReference;

import org.citrusframework.Answer;
import org.citrusframework.Question;
import org.citrusframework.TestActionRunner;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class AskActionTest extends UnitTestSupport implements TestActionSupport {

    private DefaultTestCaseRunner runner;

    @BeforeMethod
    public void setupRunner() {
        runner = new DefaultTestCaseRunner(context);
    }

    @Test
    public void shouldAnswerIntoHolder() {
        Answer<String> orderId = new Answer<>();

        runner.run(ask(new TheOrderId()).into(orderId));

        assertEquals(orderId.get(), "ORD-123");
    }

    @Test
    public void shouldReportAsAskWithQuestionName() {
        Answer<String> orderId = new Answer<>();

        runner.run(ask(new TheOrderId()).into(orderId));

        AskAction<?> action = (AskAction<?>) runner.getTestCase().getActions().get(0);
        assertEquals(action.getName(), "ask: the order id");
    }

    @Test
    public void shouldFailWithQuestionNameWhenAnsweringThrows() {
        Answer<String> holder = new Answer<>();

        Question<String> broken = Question.about("the order id", ctx -> {
            throw new IllegalStateException("no DB");
        });

        TestCaseFailedException e = expectThrows(TestCaseFailedException.class,
                () -> runner.run(ask(broken).into(holder)));
        assertTrue(e.getMessage().contains("the order id"));
    }

    @Test
    public void shouldPublishValueAsVariableWithSaveAs() {
        Answer<String> orderId = new Answer<>();

        runner.run(ask(new TheOrderId()).into(orderId).saveAs("orderId"));

        assertEquals(orderId.get(), "ORD-123");
        assertEquals(context.getVariable("orderId"), "ORD-123");

        runner.run(echo("order is ${orderId}"));
    }

    @Test
    public void shouldAnswerAgainstActiveRunnerInsideSequential() {
        AtomicReference<TestActionRunner> answeringRunner = new AtomicReference<>();
        Answer<String> holder = new Answer<>();

        Question<String> capturing = new Question<>() {
            @Override
            public String answeredBy(TestActionRunner r, TestContext ctx) {
                answeringRunner.set(r);
                return "ORD-123";
            }

            @Override
            public String getName() {
                return "the order id";
            }
        };

        runner.run(sequential().actions(
                ask(capturing).into(holder)
        ));

        assertEquals(holder.get(), "ORD-123");
        assertTrue(answeringRunner.get() != null);
    }

    @Test
    public void shouldAnswerAgainstActiveRunnerInsideBehavior() {
        AtomicReference<TestActionRunner> answeringRunner = new AtomicReference<>();
        Answer<String> holder = new Answer<>();

        Question<String> capturing = Question.about("the order id", ctx -> "ORD-123");

        Question<String> runnerCapturing = new Question<>() {
            @Override
            public String answeredBy(TestActionRunner r, TestContext ctx) {
                answeringRunner.set(r);
                return "ORD-123";
            }

            @Override
            public String getName() {
                return "the order id";
            }
        };

        runner.run(apply(r -> r.run(ask(runnerCapturing).into(holder)))) ;

        assertEquals(holder.get(), "ORD-123");
        assertTrue(answeringRunner.get() != null);

        Answer<String> nested = new Answer<>();
        runner.run(apply(r -> r.run(ask(capturing).into(nested))));
        assertEquals(nested.get(), "ORD-123");
    }

    @Test
    public void shouldRejectMissingQuestion() {
        Answer<String> holder = new Answer<>();

        AskAction<String> action = new AskAction.Builder<String>().into(holder).build();

        expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
    }

    @Test
    public void shouldRejectMissingHolder() {
        AskAction<String> action = new AskAction.Builder<String>().question(new TheOrderId()).build();

        CitrusRuntimeException e = expectThrows(CitrusRuntimeException.class, () -> action.execute(context));
        assertTrue(e.getMessage().contains("the order id"));
    }

    @Test
    public void shouldSupportPureReadWithoutRunner() {
        AskAction<String> action = new AskAction.Builder<String>()
                .question(Question.about("the order id", ctx -> "ORD-123"))
                .into(new Answer<>())
                .build();

        // no runner injected - pure read ignores it
        action.execute(context);
    }

    @Test
    public void shouldSetQuestionNameOnHolderAtBuildTime() {
        Answer<String> holder = new Answer<>();

        new AskAction.Builder<String>().question(new TheOrderId()).into(holder).build();

        try {
            holder.get();
            throw new AssertionError("Expected CitrusRuntimeException");
        } catch (CitrusRuntimeException e) {
            assertTrue(e.getMessage().contains("the order id"));
        }
    }

    @Test
    public void shouldHoldTypedValue() {
        Answer<Integer> count = new Answer<>();

        runner.run(ask(Question.about("line count", ctx -> 5)).into(count));

        assertSame(count.get(), 5);
    }

    private static class TheOrderId implements Question<String> {
        @Override
        public String answeredBy(TestActionRunner runner, TestContext context) {
            return "ORD-123";
        }
    }
}
