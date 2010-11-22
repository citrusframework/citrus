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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action used for time measurement during test. User can define a time line that is followed
 * during the test case. Action can print out the watched time to the console/logger.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class StopTimeAction extends AbstractTestAction {

    /** Static member to hold all time stamps */
    private static Map<String, Long> timeStamps = new HashMap<String, Long>();

    /** Default time stamp id */
    public static String DEFAULT_TIMELINE_ID = "CITRUS_TIMELINE";

    /** Current time line id */
    private String id = DEFAULT_TIMELINE_ID;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StopTimeAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        DecimalFormat decFormat = new DecimalFormat("0.0");
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);

        try {
            if (timeStamps.containsKey(id)) {
                if (description != null) {
                    log.info("TimeWatcher " + id + " after " + decFormat.format((System.currentTimeMillis() - timeStamps.get(id).longValue())/(double)1000) + " seconds (" + description + ")");
                } else {
                    log.info("TimeWatcher " + id + " after " + decFormat.format((System.currentTimeMillis() - timeStamps.get(id).longValue())/(double)1000) + " seconds");
                }
            } else {
                log.info("Starting TimeWatcher: " + id);
                timeStamps.put(id, Long.valueOf(System.currentTimeMillis()));
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Setter for timeline id.
     * @param period
     */
    public void setId(String period) {
        this.id = period;
    }

    /**
     * Get the current time stamps.
     * @return the timeStamps
     */
    public static Map<String, Long> getTimeStamps() {
        return timeStamps;
    }
}
