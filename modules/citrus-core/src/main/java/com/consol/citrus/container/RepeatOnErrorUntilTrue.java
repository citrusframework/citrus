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
public class RepeatOnErrorUntilTrue extends AbstractIteratingActionContainer {
    /** Auto sleep in milliseconds */
    private Long autoSleep = 1000L;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    /**
     * Default constructor.
     */
    public RepeatOnErrorUntilTrue() {
        setName("repeat-on-error");
    }

    /**
     * @see AbstractIteratingActionContainer#executeIteration(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        CitrusRuntimeException exception = null;

        while(!checkCondition(context)) {
            try {
                exception = null;
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                exception = e;

                log.info("Caught exception of type " + e.getClass().getName() + " '" +
                        e.getMessage() + "' - performing retry #" + index);

                doAutoSleep();
                index++;
            }
        }

        if (exception != null) {
            log.info("All retries failed - raising exception " + exception.getClass().getName());
            throw exception;
        }
    }

    /**
     * Sleep amount of time in between iterations.
     */
    private void doAutoSleep() {
        if (autoSleep > 0) {
            log.info("Sleeping " + autoSleep + " milliseconds");

            try {
                Thread.sleep(autoSleep);
            } catch (InterruptedException e) {
                log.error("Error during doc generation", e);
            }

            log.info("Returning after " + autoSleep + " milliseconds");
        }
    }

    /**
     * Setter for auto sleep time (in milliseconds).
     * @param autoSleep
     */
    public void setAutoSleep(Long autoSleep) {
        this.autoSleep = autoSleep;
    }

    /**
     * Gets the autoSleep.
     * @return the autoSleep
     */
    public Long getAutoSleep() {
        return autoSleep;
    }
}
