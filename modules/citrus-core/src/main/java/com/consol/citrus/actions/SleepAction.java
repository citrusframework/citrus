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
import org.springframework.util.StringUtils;

/**
 * Stop the test execution for a given amount of time.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class SleepAction extends AbstractTestAction {
    /** Delay time in seconds */
    private String seconds;

    /** Delay time in milliseconds */
    private String milliseconds = "5000";

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SleepAction.class);

    /**
     * Default constructor.
     */
    public SleepAction() {
        setName("sleep");
    }

    @Override
    public void doExecute(TestContext context) {
        String value;

        if (StringUtils.hasText(seconds)) {
            value = String.valueOf((long) (Double.valueOf(context.resolveDynamicValue(seconds)) * 1000L));
        } else {
            //check if given delay value is a variable or function
            value = context.resolveDynamicValue(milliseconds);
        }

        try {
            log.info(String.format("Sleeping %s ms", value));

            Thread.sleep(Long.valueOf(value));

            log.info(String.format("Returning after %s ms", value));
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Setter for milliseconds
     * @param milliseconds
     */
    public SleepAction setMilliseconds(String milliseconds) {
        this.milliseconds = milliseconds;
        return this;
    }

    /**
     * Gets the milliseconds.
     * @return the milliseconds
     */
    public String getMilliseconds() {
        return milliseconds;
    }

    /**
     * Setter for seconds
     * @param seconds
     */
    public SleepAction setSeconds(String seconds) {
        this.seconds = seconds;
        return this;
    }

    /**
     * Gets the seconds.
     * @return the seconds
     */
    public String getSeconds() {
        return seconds;
    }
}
