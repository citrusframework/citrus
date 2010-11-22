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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

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

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(InputAction.class);

    /** Prompted message displayed to the user before input */
    private String message = "Press return key to continue...";
    
    /** Destination variable name */
    private String variable = "userinput";

    /** Valid answers, tokenized by '/' character */
    private String validAnswers;

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {

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

                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                input = stdin.readLine();
            } while (validAnswers != null && !checkAnswer(input));
        } catch (IOException e) {
            throw new CitrusRuntimeException(e);
        }

        context.setVariable(variable, input.trim());
    }

    /**
     * Validate given input according to valid answer tokens.
     * @param input
     * @return
     */
    private boolean checkAnswer(String input) {
        StringTokenizer tok = new StringTokenizer(validAnswers, "/");

        while (tok.hasMoreTokens()) {
            if (tok.nextElement().toString().trim().toLowerCase().equals(input.trim().toLowerCase())) {
                return true;
            }
        }

        log.info("User input is not valid");

        return false;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * @param validAnswers the validAnswers to set
     */
    public void setValidAnswers(String validAnswers) {
        this.validAnswers = validAnswers;
    }

}
