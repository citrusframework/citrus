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

package com.consol.citrus.group;

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
     * @see com.consol.citrus.group.AbstractIteratingTestAction#executeIteration(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        log.info("Executing repeat-on-error loop - containing " + actions.size() + " actions");

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
