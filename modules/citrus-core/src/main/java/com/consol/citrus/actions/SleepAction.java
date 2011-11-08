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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Stop the test execution for a given amount of time.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class SleepAction extends AbstractTestAction {
    /** Delay time in seconds */
    private String delay = "5";

    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(SleepAction.class);

    @Override
    public void doExecute(TestContext context) {
        String value = null;

        //check if given delay value is a variable or function
        value = context.resolveDynamicValue(delay);

        try {
            log.info("Sleeping " + value + " seconds");

            Thread.sleep((long) (new Double(value).doubleValue() * 1000));

            log.info("Returning after " + value + " seconds");
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Setter for delay
     * @param delay
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }

    /**
     * Gets the delay.
     * @return the delay
     */
    public String getDelay() {
        return delay;
    }
}
