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

package org.citrusframework.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.StringTokenizer;
import java.util.stream.Stream;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.citrusframework.util.StringUtils;

/**
 * Test action prompts user data from standard input stream. The input data is then stored as new
 * test variable. Test workflow stops until user input is complete.
 *
 * Action can declare a set of valid answers, so user will be prompted until a valid
 * answer was returned.
 *
 * @author Christoph Deppisch
 */
public class InputAction extends AbstractTestAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(InputAction.class);

    /** Prompted message displayed to the user before input */
    private final String message;

    /** Destination variable name */
    private final String variable;

    /** Valid answers, tokenized by '/' character */
    private final String validAnswers;

    /** Reader providing the user input */
    private final BufferedReader inputReader;

    /** Separates valid answer possibilities */
    public static final String ANSWER_SEPARATOR = "/";

    /**
     * Default constructor.
     */
    public InputAction(Builder builder) {
        super("input", builder);

        this.message = builder.message;
        this.variable = builder.variable;
        this.validAnswers = builder.validAnswers;
        this.inputReader = Optional.ofNullable(builder.inputReader).orElseGet(() -> new BufferedReader(new InputStreamReader(System.in)));
    }

    @Override
    public void doExecute(TestContext context) {
        String input;

        if (context.getVariables().containsKey(variable)) {
            input = context.getVariable(variable);
            logger.info("Variable " + variable + " is already set (='" + input + "'). Skip waiting for user input");

            return;
        }

        String display;
        if (StringUtils.hasText(validAnswers)) {
            display = message + " (" + validAnswers + ")";
        } else {
            display = message;
        }


        try (BufferedReader stdin = getInputReader()) {
            do {
                logger.info(display);
                input = stdin.readLine();
            } while (validAnswers != null && !checkAnswer(input));
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        context.setVariable(variable, input.trim());
    }

    /**
     * Provides input stream reader from system in standard input stream.
     * @return
     */
    protected BufferedReader getInputReader() {
        return inputReader;
    }

    /**
     * Validate given input according to valid answer tokens.
     * @param input
     * @return
     */
    private boolean checkAnswer(String input) {
        StringTokenizer tok = new StringTokenizer(validAnswers, ANSWER_SEPARATOR);

        while (tok.hasMoreTokens()) {
            if (tok.nextElement().toString().trim().equalsIgnoreCase(input.trim())) {
                return true;
            }
        }

        logger.info("User input is not valid - must be one of " + validAnswers);

        return false;
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the variable.
     * @return the variable
     */
    public String getVariable() {
        return variable;
    }

    /**
     * Gets the validAnswers.
     * @return the validAnswers
     */
    public String getValidAnswers() {
        return validAnswers;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<InputAction, Builder> {

        private String message = "Press return key to continue ...";
        private String variable = "userinput";
        private String validAnswers;
        private BufferedReader inputReader;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder input() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param message
         * @return
         */
        public static Builder input(String message) {
            Builder builder = new Builder();
            builder.message(message);
            return builder;
        }

        /**
         * Sets the message displayed to the user.
         * @param message the message to set
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Stores the result to a test variable.
         * @param variable the variable to set
         */
        public Builder result(String variable) {
            this.variable = variable;
            return this;
        }

        /**
         * Sets the input reader.
         * @param reader the input reader to set
         */
        public Builder reader(BufferedReader reader) {
            this.inputReader = reader;
            return this;
        }

        /**
         * Sets the valid answers.
         * @param answers the validAnswers to set
         */
        public Builder answers(String... answers) {
            if (answers.length == 0) {
                throw new CitrusRuntimeException("Please specify proper answer possibilities for input action");
            }

            StringJoiner joiner = new StringJoiner(InputAction.ANSWER_SEPARATOR);
            Stream.of(answers).forEach(joiner::add);
            this.validAnswers = joiner.toString();
            return this;
        }

        @Override
        public InputAction build() {
            return new InputAction(this);
        }
    }
}
