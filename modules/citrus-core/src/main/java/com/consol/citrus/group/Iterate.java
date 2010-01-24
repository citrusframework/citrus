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
 * Class executes nested test actions in loops. Iteration continues as long
 * as looping condition evaluates to true.
 * 
 * See {@link BooleanExpressionParser} for supported boolean expressions that define
 * the conditioning.
 * 
 * Each loop an index variable is incremented. The index variable is accessible inside the nested
 * test actions as normal test variable. Iteration starts with index=1 and increments with a 
 * default step=1.
 * 
 * @author Christoph Deppisch
 */
public class Iterate extends AbstractTestAction {
    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    /** Boolean expression string */
    private String condition;

    /** Name of index variable */
    private String indexName;

    /** Looping index */
    private int index = 1;

    /** Index increment step */
    private int step = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Iterate.class);

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

        while (checkCondition()) {
            executeActions(context);

            index = index + step ;
        }
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
     * Check aborting condition.
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
     * Nested test actions to execute.
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    /**
     * Aborting condition.
     * @param condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Name of index variable.
     * @param indexName
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    /**
     * Step o increment.
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }

    /**
     * Setter for looping index.
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
