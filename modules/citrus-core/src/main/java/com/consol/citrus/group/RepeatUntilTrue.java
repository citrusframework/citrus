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

package com.consol.citrus.group;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.BooleanExpressionParser;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 * 
 * Index is incremented each iteration and stored as test variable accessible in the nested test actions
 * as normal variable. Index starts with 1 by default.
 * 
 * @author Christoph Deppisch
 */
public class RepeatUntilTrue extends AbstractTestAction {
    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    /** Aborting condition */
    private String condition;

    /** Name of index variable */
    private String indexName;

    /** Iteration index */
    private int index = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(RepeatUntilTrue.class);

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Executing iterate loop - containing " + actions.size() + " actions");

        try {
            condition = context.replaceDynamicContentInString(condition);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        do {
            executeActions(context);
            index++;
        } while (!checkCondition());
    }

    /**
     * Executes the nested test actions.
     * @param context
     */
    private void executeActions(TestContext context) {
        context.setVariable(indexName, Integer.valueOf(index).toString());

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = actions.get(i);

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        }
    }

    /**
     * Check the aborting condition.
     * @return
     */
    private boolean checkCondition() {
        String conditionString = condition;

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, Integer.valueOf(index).toString());
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }

    /**
     * List of nested test actions.
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * Iteration aborting condition.
     * @param condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Index variable name.
     * @param indexName
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
}
