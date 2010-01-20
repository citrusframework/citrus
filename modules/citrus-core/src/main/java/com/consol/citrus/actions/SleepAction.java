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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Lets the whole test execution sleep for an amount of time
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class SleepAction extends AbstractTestAction {
    /** Amount of time in seconds */
    private String delay;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SleepAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        String value = null;

        if (VariableUtils.isVariableName(delay)) {
            value = context.getVariable(delay);
        } else if(context.getFunctionRegistry().isFunction(delay)) {
            value = FunctionUtils.resolveFunction(delay, context);
        } else {
            value = delay;
        }

        try {
            log.info("Sleeping " + value + " seconds");

            Thread.sleep((long)(new Double(value).doubleValue() * 1000));

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
}
