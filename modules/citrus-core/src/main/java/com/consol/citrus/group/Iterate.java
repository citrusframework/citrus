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
public class Iterate extends AbstractIteratingTestAction {
    /** Index increment step */
    private int step = 1;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(Iterate.class);

    @Override
    public void executeIteration(TestContext context) {
        log.info("Executing iterate loop - containing " + actions.size() + " actions");

        while (checkCondition()) {
            executeActions(context);

            index = index + step ;
        }
    }

    /**
     * Step o increment.
     * @param step the step to set
     */
    public void setStep(int step) {
        this.step = step;
    }
}
