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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Looping test container iterating the nested test actions in case an error occurred in one
 * of them. Iteration continues until a aborting condition evaluates to true.
 * 
 * Number of iterations is kept in a index variable. The nested test actions can access this variable
 * as normal test variable.
 * 
 * Between the iterations container can sleep automatically a given amount of time.
 * 
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrue extends AbstractIteratingTestAction {
    /** Auto sleep in seconds */
    private int autoSleep = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    /**
     * @see com.consol.citrus.container.AbstractIteratingTestAction#executeIteration(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        do {
            try {
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                index++;
                if (checkCondition()) {
                    throw new CitrusRuntimeException(e);
                } else {
                    index--;
                    log.info("Caught exception of type " + e.getClass().getName() + " '" + e.getMessage() + "' - repeating because of error");
                }
            } finally {
                index++;
            }
        } while (!checkCondition());
    }

    /**
     * Executes the nested test actions.
     * @param context
     */
    protected void executeActions(TestContext context) {
        if (autoSleep > 0) {
            log.info("Sleeping " + autoSleep + " seconds");

            try {
                Thread.sleep(autoSleep * 1000);
            } catch (InterruptedException e) {
                log.error("Error during doc generation", e);
            }

            log.info("Returning after " + autoSleep + " seconds");
        }

        super.executeActions(context);
    }

    /**
     * Setter for auto sleep time (in seconds).
     * @param autoSleep
     */
    public void setAutoSleep(int autoSleep) {
        this.autoSleep = autoSleep;
    }
}
