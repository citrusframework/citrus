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

package com.consol.citrus.container;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;

/**
 * Sequence to perform a block of other actions in sequence
 *
 * @author deppisch Christoph Deppisch Consol* Software GmbH 2007
 */
public class Sequence extends AbstractTestAction {

    /** List of actions to be executed */
    private List<TestAction> actions = new ArrayList<TestAction>();

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Sequence.class);

    /*
     * (non-Javadoc)
     * @see com.consol.citrus.TestAction#execute()
     */
    @Override
    public void execute(TestContext context) {
        log.info("Executing action sequence - containing " + actions.size() + " actions");

        for (int i = 0; i < actions.size(); i++) {
            TestAction action = actions.get(i);

            if(log.isDebugEnabled()) {
                log.debug("Executing action " + action.getClass().getName());
            }
            action.execute(context);
        }

        log.info("Action sequence finished successfully");
    }

    /**
     * @param actions
     */
    public void setActions(List<TestAction> actions) {
        this.actions = actions;
    }
}
