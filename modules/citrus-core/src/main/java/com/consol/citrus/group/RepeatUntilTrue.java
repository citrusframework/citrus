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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;

/**
 * Typical implementation of repeat iteration loop. Nested test actions are executed until
 * aborting condition evaluates to true.
 * 
 * Index is incremented each iteration and stored as test variable accessible in the nested test actions
 * as normal variable. Index starts with 1 by default.
 * 
 * @author Christoph Deppisch
 */
public class RepeatUntilTrue extends AbstractIteratingTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(RepeatUntilTrue.class);

    @Override
    public void executeIteration(TestContext context) {
        log.info("Executing iterate loop - containing " + actions.size() + " actions");

        do {
            executeActions(context);
            index++;
        } while (!checkCondition());
    }
}
