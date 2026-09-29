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

import java.util.function.Supplier;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Single-assignment holder for the typed answer of a {@link Question}.
 * <p>
 * The holder exists because {@link TestActionRunner#run(TestActionBuilder)} returns the runner
 * for chaining, never the built action, so a value cannot travel back to the test by return value.
 * A {@code null} answer is a valid answer and is distinguishable from "not answered yet".
 */
public final class Answer<T> implements Supplier<T> {

    private volatile boolean answered;
    private volatile T value;
    private volatile String questionName;

    /**
     * Returns the answer.
     * @return the answer, may be null when answered with null.
     * @throws CitrusRuntimeException when not answered yet, naming the question when known.
     */
    @Override
    public T get() {
        if (!answered) {
            if (questionName != null) {
                throw new CitrusRuntimeException(
                        "Question '%s' has not been answered yet".formatted(questionName));
            }

            throw new CitrusRuntimeException("Answer has not been set yet - ask the question first");
        }

        return value;
    }

    /**
     * Assigns the answer. Called by the asking action, once.
     * @param value the answer, may be null.
     * @throws CitrusRuntimeException when assigned a second time.
     */
    public void set(T value) {
        if (answered) {
            if (questionName != null) {
                throw new CitrusRuntimeException(
                        "Question '%s' has already been answered".formatted(questionName));
            }

            throw new CitrusRuntimeException("Answer has already been set");
        }

        this.value = value;
        this.answered = true;
    }

    /**
     * Names the question this holder waits for. Called by the asking action when built,
     * so reading too early names the question instead of returning null.
     * @param questionName the question name.
     */
    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    /**
     * Whether the question has been answered, including an answer of null.
     * @return true when answered.
     */
    public boolean isAnswered() {
        return answered;
    }

    @Override
    public String toString() {
        if (!answered) {
            return "unanswered";
        }

        return String.valueOf(value);
    }
}
