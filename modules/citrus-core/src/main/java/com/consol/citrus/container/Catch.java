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

package com.consol.citrus.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action catches possible exceptions in nested test actions.
 * 
 * @author Christoph Deppisch
 */
public class Catch extends AbstractActionContainer {
    /** Exception type caught */
    private String exception = CitrusRuntimeException.class.getName();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Catch.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) {
        log.debug("Catch container catching exceptions of type " + exception);

        for (TestAction action: actions) {
            try {
                action.execute(context);
            } catch (Exception e) {
                if (exception != null && exception.equals(e.getClass().getName())) {
                    log.info("Caught exception " + e.getClass() + ": " + e.getLocalizedMessage());
                    continue;
                }
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Set the exception that is caught.
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }
}
