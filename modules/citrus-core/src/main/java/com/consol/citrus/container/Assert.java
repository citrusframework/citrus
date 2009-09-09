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

package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class Assert extends AbstractTestAction {
    /** TestAction to be executed */
    private TestAction action;

    /** Aserted exception */
    private String exception = CitrusRuntimeException.class.getName();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Assert.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Assert container asserting exceptions of type " + exception);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        } catch (Exception e) {
            log.info("Validating caught exception ...");
            if (exception != null) {
                if (exception.equals(e.getClass().getName())) {
                    log.info("Exception is " + e.getClass() + ": " + e.getLocalizedMessage());
                    log.info("Exception validation OK");
                    return;
                } else {
                    throw new CitrusRuntimeException("Caught exception does not fit expected exception type '" + exception + "' caught: " + e.getClass().getName());
                }
            }
        }

        throw new CitrusRuntimeException("Asserted exception " + exception + " was not thrown as expected");
    }

    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * @param action the action to set
     */
    public void setAction(TestAction action) {
        this.action = action;
    }
}
