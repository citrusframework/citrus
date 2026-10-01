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
import org.citrusframework.message.MessageStore;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

public class QuestionTest {

    private final TestActionRunner runner = mock(TestActionRunner.class);
    private final TestContext context = mock(TestContext.class);
    private final MessageStore messageStore = mock(MessageStore.class);

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

    @Test
    public void shouldReadVariableWithoutContextInternals() {
        when(context.getVariable("orderId", String.class)).thenReturn("ORD-123");

        Question<String> question = Question.variable("orderId", String.class);

        assertEquals(question.getName(), "variable orderId");
        assertEquals(question.answeredBy(runner, context), "ORD-123");
    }

    @Test
    public void shouldReadMessageFromStore() {
        org.citrusframework.message.Message message =
                new org.citrusframework.message.DefaultMessage("ORD-123");
        when(context.getMessageStore()).thenReturn(messageStore);
        when(messageStore.getMessage("orders")).thenReturn(message);

        Question<org.citrusframework.message.Message> question = Question.message("orders");

        assertEquals(question.getName(), "message orders");
        assertEquals(question.answeredBy(runner, context), message);
    }

    @Test
    public void shouldFailForUnknownMessage() {
        when(context.getMessageStore()).thenReturn(messageStore);
        when(messageStore.getMessage("ordres")).thenReturn(null);

        Question<org.citrusframework.message.Message> question = Question.message("ordres");

        try {
            question.answeredBy(runner, context);
            throw new AssertionError("Expected CitrusRuntimeException");
        } catch (org.citrusframework.exceptions.CitrusRuntimeException e) {
            assertTrue(e.getMessage().contains("ordres"));
        }
    }

    @Test
    public void shouldRejectMissingVariableInput() {
        assertThrows(IllegalArgumentException.class, () -> Question.variable(null, String.class));
        assertThrows(IllegalArgumentException.class, () -> Question.variable(" ", String.class));
        assertThrows(NullPointerException.class, () -> Question.variable("orderId", null));
    }

    @Test
    public void shouldRejectMissingMessageName() {
        assertThrows(IllegalArgumentException.class, () -> Question.message(null));
        assertThrows(IllegalArgumentException.class, () -> Question.message(" "));
    }

    private static class TheOrderId implements Question<String> {
        @Override
        public String answeredBy(TestActionRunner runner, TestContext context) {
            return "ORD-123";
        }
    }
}
