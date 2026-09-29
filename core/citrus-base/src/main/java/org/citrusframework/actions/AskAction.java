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

import java.util.Optional;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.Answer;
import org.citrusframework.Question;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionRunnerAware;
import org.citrusframework.api.actions.AskActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Runs a {@link Question} as an ordinary test action and hands the typed answer back
 * through an {@link Answer} holder, optionally publishing it as a test variable.
 * <p>
 * The action is reported as {@code ask: <question name>} at the position it ran.
 */
public class AskAction<T> extends AbstractTestAction {

    private final Question<T> question;
    private final Answer<T> holder;
    private final String saveAs;
    private final TestActionRunner runner;

    public AskAction(Builder<T> builder) {
        super(Optional.ofNullable(builder.question)
                .map(Question::getName)
                .filter(name -> name != null && !name.isBlank())
                .map(name -> "ask: " + name)
                .orElse("ask"), builder);

        this.question = builder.question;
        this.holder = builder.holder;
        this.saveAs = builder.saveAs;
        this.runner = builder.runner;

        if (this.question != null && this.holder != null) {
            this.holder.setQuestionName(this.question.getName());
        }
    }

    @Override
    public void doExecute(TestContext context) {
        if (question == null) {
            throw new CitrusRuntimeException("Missing question to ask");
        }

        if (holder == null) {
            throw new CitrusRuntimeException("Missing answer holder - use into(..) to receive the answer of question '%s'".formatted(question.getName()));
        }

        final T value;
        try {
            value = question.answeredBy(runner, context);
        } catch (Exception | Error e) {
            throw new CitrusRuntimeException(
                    "Failed to answer question '%s': %s".formatted(question.getName(), e.getMessage()), e);
        }

        holder.set(value);

        if (saveAs != null && value != null) {
            context.setVariable(saveAs, value);
        }
    }

    public Question<T> getQuestion() {
        return question;
    }

    public Answer<T> getHolder() {
        return holder;
    }

    public String getSaveAs() {
        return saveAs;
    }

    /**
     * Action builder.
     */
    public static final class Builder<T> extends AbstractTestActionBuilder<AskAction<T>, Builder<T>>
            implements AskActionBuilder<T, AskAction<T>>, TestActionRunnerAware {

        private Question<T> question;
        private Answer<T> holder;
        private String saveAs;
        private TestActionRunner runner;

        public static <T> Builder<T> ask() {
            return new Builder<>();
        }

        public static <T> Builder<T> ask(Question<T> question) {
            Builder<T> builder = new Builder<>();
            builder.question = question;
            return builder;
        }

        @Override
        public Builder<T> question(Question<T> question) {
            this.question = question;
            return this;
        }

        @Override
        public Builder<T> into(Answer<T> holder) {
            this.holder = holder;
            return this;
        }

        @Override
        public Builder<T> saveAs(String variableName) {
            this.saveAs = variableName;
            return this;
        }

        @Override
        public void setTestActionRunner(TestActionRunner runner) {
            this.runner = runner;
        }

        @Override
        public AskAction<T> build() {
            return new AskAction<>(this);
        }
    }
}
