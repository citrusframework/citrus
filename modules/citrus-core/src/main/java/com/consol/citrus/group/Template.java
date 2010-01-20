/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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

package com.consol.citrus.group;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.VariableUtils;

/**
 * Template to perform a block of other actions in sequence
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class Template extends AbstractTestAction {

    /** Name of sequence */
    private String name;

    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    private Map<String, String> parameter = new LinkedHashMap<String, String>();
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
    private boolean globalContext = true;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Template.class);

    @Override
    public void execute(TestContext context) {
        log.info("Executing action template '" + name + "' - containing " + actions.size() + " actions");

        TestContext innerContext;
        
        //decide wheather to use global test context or not
        if(globalContext) {
            innerContext = context;
        } else {
            innerContext = new TestContext();
            innerContext.setFunctionRegistry(context.getFunctionRegistry());
            GlobalVariables globalVariables = new GlobalVariables();
            globalVariables.getVariables().putAll(context.getGlobalVariables());
            innerContext.setGlobalVariables(globalVariables);
            innerContext.getVariables().putAll(context.getVariables());
        }
        
        for (Entry<String, String> entry : parameter.entrySet()) {
            String param = entry.getKey();
            String paramValue = entry.getValue();

            if (VariableUtils.isVariableName(paramValue)) {
                paramValue = context.getVariable(paramValue);
            } else if(functionRegistry.isFunction(paramValue)) {
                paramValue = FunctionUtils.resolveFunction(paramValue, context);
            } 

            log.info("Setting parameter for template " + param + "=" + paramValue);

            innerContext.setVariable(param, paramValue);
        }

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = actions.get(i);

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(innerContext);
        }

        log.info("Template was executed successfully");
    }

    /**
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    /**
     * @param globalContext the globalContext to set
     */
    public void setGlobalContext(boolean globalContext) {
        this.globalContext = globalContext;
    }
}
