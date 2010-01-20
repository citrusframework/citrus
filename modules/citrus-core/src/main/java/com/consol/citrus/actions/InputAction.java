/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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

public class InputAction extends AbstractTestAction {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(InputAction.class);

    private String message = "Press return key to continue...";

    private String variable = "userinput";

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
