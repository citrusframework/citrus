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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class Catch extends AbstractTestAction {
    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    /** Exception type to be caught */
    private String exception = CitrusRuntimeException.class.getName();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Catch.class);

    /**
     * @see com.consol.citrus.TestAction#execute()
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.debug("Catch container catching exceptions of type " + exception);

        for (int i = 0; i < actions.size(); i++) {
            try {
                TestAction action = actions.get(i);

                if (log.isDebugEnabled()) {
                    log.debug("Executing action " + action.getClass().getName());
                }

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
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }
}
