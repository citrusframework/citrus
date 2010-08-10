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

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;

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
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Assert.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) {
        log.info("Assert container asserting exceptions of type " + exception);

        try {
            setLastExecutedAction(action);
            action.execute(context);
        } catch (Exception e) {
            log.info("Validating caught exception ...");
            if (exception.isAssignableFrom(e.getClass())) {
                
                if(message != null && !message.equals(e.getLocalizedMessage())) {
                    throw new ValidationException("Validation failed for asserted exception message - expected: '" + message + "' but was: '" + e.getLocalizedMessage() + "'");
                }
                
                log.info("Exception is as expected: " + e.getClass() + ": " + e.getLocalizedMessage());
                log.info("Exception validation successful");
                return;
            } else {
                throw new ValidationException("Validation failed for asserted exception type - expected: '" + exception + "' but was: '" + e.getClass().getName() + "'");
            }
        }

        throw new ValidationException("Missing asserted exception '" + exception + "'");
    }

    /**
     * Set the exception.
     * @param exception the exception to set
     */
    public void setException(Class<Throwable> exception) {
        this.exception = exception;
    }

    /**
     * Set the nested test action.
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
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
     * @see com.consol.citrus.container.TestActionContainer#addTestAction(com.consol.citrus.TestAction)
     */
    public void addTestAction(TestAction action) {
        this.action = action;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionCount()
     */
    public long getActionCount() {
        return 1;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActionIndex(com.consol.citrus.TestAction)
     */
    public int getActionIndex(TestAction action) {
        return 0;
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getActions()
     */
    public List<TestAction> getActions() {
        return Collections.singletonList(action);
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#getTestAction(int)
     */
    public TestAction getTestAction(int index) {
        if(index == 0) {
            return action;
        } else {
            throw new IndexOutOfBoundsException("Illegal index in action list:" + index);
        }
    }

    /**
     * @see com.consol.citrus.container.TestActionContainer#setActions(java.util.List)
     */
    public void setActions(List<TestAction> actions) {
        if(!CollectionUtils.isEmpty(actions)) {
            action = actions.get(0); 
        }
    }
}
