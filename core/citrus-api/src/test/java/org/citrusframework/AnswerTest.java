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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AnswerTest {

    @Test
    public void shouldFailWhenReadBeforeAnswered() {
        Answer<String> answer = new Answer<>();

        assertFalse(answer.isAnswered());
        assertThrows(CitrusRuntimeException.class, answer::get);
    }

    @Test
    public void shouldNameQuestionWhenReadBeforeAnswered() {
        Answer<String> answer = new Answer<>();
        answer.setQuestionName("the order id");

        try {
            answer.get();
            throw new AssertionError("Expected CitrusRuntimeException");
        } catch (CitrusRuntimeException e) {
            assertTrue(e.getMessage().contains("the order id"));
        }
    }

    @Test
    public void shouldFailOnSecondAssignment() {
        Answer<String> answer = new Answer<>();
        answer.set("ORD-123");

        try {
            answer.set("ORD-456");
            throw new AssertionError("Expected CitrusRuntimeException");
        } catch (CitrusRuntimeException e) {
            assertTrue(e.getMessage().contains("already"));
        }
    }

    @Test
    public void shouldNameQuestionOnSecondAssignment() {
        Answer<String> answer = new Answer<>();
        answer.setQuestionName("the order id");
        answer.set("ORD-123");

        try {
            answer.set("ORD-456");
            throw new AssertionError("Expected CitrusRuntimeException");
        } catch (CitrusRuntimeException e) {
            assertTrue(e.getMessage().contains("the order id"));
        }
    }

    @Test
    public void shouldDistinguishNullAnswerFromNotAnswered() {
        Answer<String> answer = new Answer<>();
        answer.set(null);

        assertTrue(answer.isAnswered());
        assertNull(answer.get());
    }

    @Test
    public void shouldHoldTypedValue() {
        Answer<Integer> answer = new Answer<>();
        answer.set(5);

        assertTrue(answer.isAnswered());
        assertEquals(Integer.valueOf(5), answer.get());
    }

    @Test
    public void shouldAssignOnlyOnceUnderConcurrency() throws InterruptedException {
        Answer<String> answer = new Answer<>();
        answer.setQuestionName("the order id");

        java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger failures = new java.util.concurrent.atomic.AtomicInteger();

        Runnable assign = () -> {
            ready.countDown();
            try {
                start.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            try {
                answer.set("ORD-123");
            } catch (CitrusRuntimeException e) {
                failures.incrementAndGet();
            }
        };

        Thread first = new Thread(assign);
        Thread second = new Thread(assign);
        first.start();
        second.start();

        ready.await();
        start.countDown();
        first.join();
        second.join();

        assertTrue(answer.isAnswered());
        assertEquals("ORD-123", answer.get());
        assertEquals(1, failures.get());
    }
}
