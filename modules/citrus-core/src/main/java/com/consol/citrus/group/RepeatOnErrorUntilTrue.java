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

public class RepeatOnErrorUntilTrue extends AbstractTestAction {
    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    private String condition;

    private String indexName;

    private int autoSleep = 1;

    private int index = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(RepeatOnErrorUntilTrue.class);

    /**
     * @see com.consol.citrus.actions.AbstractTestAction#execute(com.consol.citrus.context.TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        log.info("Executing repeat-on-error loop - containing " + actions.size() + " actions");

        try {
            condition = context.replaceDynamicContentInString(condition);
        } catch (ParseException e) {
            throw new CitrusRuntimeException(e);
        }

        do {
            try {
                executeActions(context);
                break;
            } catch (CitrusRuntimeException e) {
                index++;
                if (checkCondition()) {
                    throw new CitrusRuntimeException(e);
                } else {
                    index--;
                    log.info("Caught exception of type " + e.getClass().getName() + " '" + e.getMessage() + "' - repeating because of error");
                }
            } finally {
                index++;
            }
        } while (!checkCondition());
    }

    private void executeActions(TestContext context) {
        context.setVariable(indexName, Integer.valueOf(index).toString());

        if (autoSleep > 0) {
            log.info("Sleeping " + autoSleep + " seconds");

            try {
                Thread.sleep(autoSleep * 1000);
            } catch (InterruptedException e) {
                log.error("Error during doc generation", e);
            }

            log.info("Returning after " + autoSleep + " seconds");
        }

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = actions.get(i);

            if (log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }

            action.execute(context);
        }
    }

    private boolean checkCondition() {
        String conditionString = condition;

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, Integer.valueOf(index).toString());
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }

    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setAutoSleep(int autoSleep) {
        this.autoSleep = autoSleep;
    }
}
