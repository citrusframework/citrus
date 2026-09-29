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
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertThrows;

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
        assertEquals(answer.get(), Integer.valueOf(5));
    }
}
