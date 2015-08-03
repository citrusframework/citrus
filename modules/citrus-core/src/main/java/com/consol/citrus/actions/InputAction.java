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

package com.consol.citrus.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.StringTokenizer;

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
    private static Logger log = LoggerFactory.getLogger(InputAction.class);

    /** Prompted message displayed to the user before input */
    private String message = "Press return key to continue ...";
    
    /** Destination variable name */
    private String variable = "userinput";

    /** Valid answers, tokenized by '/' character */
    private String validAnswers;
    
    /** Separates valid answer possibilities */
    public static final String ANSWER_SEPARATOR = "/";

    /**
     * Default constructor.
     */
    public InputAction() {
        setName("input");
    }

    @Override
    public void doExecute(TestContext context) {

        String input = null;

        if (context.getVariables().containsKey(variable)) {
            input = context.getVariable(variable);
            log.info("Variable " + variable + " is already set (='" + input + "'). Skip waiting for user input");

            return;
        }

        String display;
        if (StringUtils.hasText(validAnswers)) {
            display = message + " (" + validAnswers + ")";
        } else {
            display = message;
        }


        try {
            do {
                log.info(display);

                BufferedReader stdin = getInputReader();
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
        return new BufferedReader(new InputStreamReader(System.in));
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

        log.info("User input is not valid");

        return false;
    }

    /**
     * Sets the message.
     * @param message the message to set
     */
    public InputAction setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the variable.
     * @param variable the variable to set
     */
    public InputAction setVariable(String variable) {
        this.variable = variable;
        return this;
    }

    /**
     * Sets the valid answers.
     * @param validAnswers the validAnswers to set
     */
    public InputAction setValidAnswers(String validAnswers) {
        this.validAnswers = validAnswers;
        return this;
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

}
