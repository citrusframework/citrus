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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

/**
 * Action that prints out variable values. Action requires a list of variable
 * names that are printed to the console with its according value.
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2006
 */
public class TraceVariablesAction extends AbstractTestAction {
    /** Values to be validated */
    private List<String> variableNames;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(TraceVariablesAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        boolean isSuccess = true;

        Iterator<String> it;
        if (variableNames != null && variableNames.size() > 0) {
            log.info("Validating variables using custom map:");
            
            it = variableNames.iterator();
        } else {
            log.info("Validating all variables in context:");
            
            it = context.getVariables().keySet().iterator();
        }

        while (it.hasNext()) {
            String key = it.next();
            String value = context.getVariable(key);

            log.info("Current value of variable " + key + " = " + value);

            if (value == null)  {
                log.info("Validation error: Variable value is null");
                
                isSuccess = false;
            }
        }

        if (!isSuccess) {
            throw new CitrusRuntimeException("Validation error, because one or more variables are null");
        }
    }

    /**
     * Setter for info values list
     * @param variableNames
     */
    public void setVariableNames(List<String> variableNames) {
        this.variableNames = variableNames;
    }
}
