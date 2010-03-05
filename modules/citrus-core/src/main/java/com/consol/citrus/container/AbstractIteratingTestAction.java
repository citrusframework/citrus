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

import java.text.ParseException;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.BooleanExpressionParser;

/**
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractIteratingTestAction extends AbstractActionContainer {
    /** Boolean expression string */
    protected String condition;

    /** Name of index variable */
    protected String indexName;

    /** Looping index */
    protected int index = 1;
    
    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     */
    @Override
    public void execute(TestContext context) {
        try {
            condition = context.replaceDynamicContentInString(condition);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }
        
        executeIteration(context);
    }
    
    /**
     * Execute embedded actions in loop.
     * @param context TestContext holding variable information.
     */
    protected abstract void executeIteration(TestContext context) throws CitrusRuntimeException;
    
    /**
     * Executes the nested test actions.
     * @param context
     */
    protected void executeActions(TestContext context) {
        context.setVariable(indexName, Integer.valueOf(index).toString());

        for (TestAction action: actions) {
            action.execute(context);
        }
    }
    
    /** 
     * Check aborting condition.
     * @return
     */
    protected boolean checkCondition() {
        String conditionString = condition;

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, Integer.valueOf(index).toString());
        }

        return BooleanExpressionParser.evaluate(conditionString);
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
     * Setter for looping index.
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
