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

package org.citrusframework;

import org.citrusframework.context.TestContext;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class QuestionTest {

    private final TestActionRunner runner = mock(TestActionRunner.class);
    private final TestContext context = mock(TestContext.class);

    @Test
    public void shouldStayAssignableFromLambda() {
        Question<String> question = (r, ctx) -> "answer";

        assertEquals(question.answeredBy(runner, context), "answer");
    }

    @Test
    public void shouldNameNamedClass() {
        assertEquals(new TheOrderId().getName(), "the order id");
    }

    @Test
    public void shouldNameLambdaGenerically() {
        Question<String> question = (r, ctx) -> "answer";

        assertEquals(question.getName(), "behavior");
    }

    @Test
    public void shouldNameAnonymousClassGenerically() {
        Question<String> question = new Question<>() {
            @Override
            public String answeredBy(TestActionRunner runner, TestContext context) {
                return "answer";
            }
        };

        assertEquals(question.getName(), "behavior");
    }

    @Test
    public void shouldUseExplicitNameForPureRead() {
        Question<String> question = Question.about("the order id", ctx -> "ORD-123");

        assertEquals(question.getName(), "the order id");
        assertEquals(question.answeredBy(runner, context), "ORD-123");
    }

    @Test
    public void shouldRejectMissingName() {
        assertThrows(IllegalArgumentException.class, () -> Question.about(" ", ctx -> "answer"));
        assertThrows(NullPointerException.class, () -> Question.about("the order id", null));
    }

    private static class TheOrderId implements Question<String> {
        @Override
        public String answeredBy(TestActionRunner runner, TestContext context) {
            return "ORD-123";
        }
    }
}
