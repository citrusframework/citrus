/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
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
 * Action used for time measurement during test
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class StopTimeAction extends AbstractTestAction {

    /** Static member to hold all time stamps */
    private static Map<String, Long> timeStamps = new HashMap<String, Long>();

    /** Default time stamp id */
    public static String DEFAULT_TIMELINE_ID = "CITRUS_TIMELINE";

    /** Id of the time measurement */
    private String id = DEFAULT_TIMELINE_ID;

    /** Description of time line */
    private String description;

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
     * Setter for id
     * @param period
     */
    public void setId(String period) {
        this.id = period;
    }

    /**
     * Setter for description
     * @param description
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the timeStamps
     */
    public static Map<String, Long> getTimeStamps() {
        return timeStamps;
    }
}
