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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Assert exception to happen in nested test action.
 * 
 * @author Christoph Deppisch
 * @since 2009
 */
public class Assert extends AbstractActionContainer {
    /** Nested test action */
    private TestAction action;

    /** Asserted exception */
    private Class<? extends Throwable> exception = CitrusRuntimeException.class;

    /** Localized exception message for control */
    private String message = null;
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Assert.class);

    /**
     * Default constructor.
     */
    public Assert() {
        setName("assert");
    }

    @Override
    public void doExecute(TestContext context) {
        log.info("Assert container asserting exceptions of type " + exception);

        try {
            setLastExecutedAction(action);
            action.execute(context);
        } catch (Exception e) {
            log.info("Validating caught exception ...");
            
            if (!exception.isAssignableFrom(e.getClass())) {
                throw new ValidationException("Validation failed for asserted exception type - expected: '" + 
                        exception + "' but was: '" + e.getClass().getName() + "'", e);
            }    
            
            if (message != null) {
                if (ValidationMatcherUtils.isValidationMatcherExpression(message)) {
                    ValidationMatcherUtils.resolveValidationMatcher("message", e.getLocalizedMessage(), message, context);
                } else if(!context.replaceDynamicContentInString(message).equals(e.getLocalizedMessage())) {
                    throw new ValidationException("Validation failed for asserted exception message - expected: '" + 
                        message + "' but was: '" + e.getLocalizedMessage() + "'", e);
                }
            }
            
            log.info("Exception is as expected: " + e.getClass() + ": " + e.getLocalizedMessage());
            log.info("Exception validation successful");
            return;
        }

        throw new ValidationException("Missing asserted exception '" + exception + "'");
    }

    /**
     * Set the nested test action.
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        addTestAction(action);
    }
    
    /**
     * Gets the action.
     * @return the action
     */
    public TestAction getAction() {
        return action;
    }

    /**
     * Set the message to send.
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the message to send.
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Gets the exception.
     * @return the exception
     */
    public Class<? extends Throwable> getException() {
        return exception;
    }

    /**
     * Sets the exception.
     * @param exception the exception to set
     */
    public void setException(Class<? extends Throwable> exception) {
        this.exception = exception;
    }

    @Override
    public Assert addTestAction(TestAction action) {
        this.action = action;
        super.addTestAction(action);
        return this;
    }

    @Override
    public TestAction getTestAction(int index) {
        if (index == 0) {
            return action;
        } else {
            throw new IndexOutOfBoundsException("Illegal index in action list:" + index);
        }
    }

    @Override
    public Assert setActions(List<TestAction> actions) {
        if (actions.size() > 1) {
            throw new CitrusRuntimeException("Invalid number of nested test actions - only one single nested action is allowed");
        }

        action = actions.get(0);
        super.setActions(actions);
        return this;
    }

}
