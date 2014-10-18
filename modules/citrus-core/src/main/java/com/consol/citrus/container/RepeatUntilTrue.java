/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.container;

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
public class RepeatUntilTrue extends AbstractIteratingActionContainer {

    /**
     * Default constructor.
     */
    public RepeatUntilTrue() {
        setName("repeat");
    }

    /**
     * @see AbstractIteratingActionContainer#executeIteration(com.consol.citrus.context.TestContext)
     * @throws com.consol.citrus.exceptions.CitrusRuntimeException
     */
    @Override
    public void executeIteration(TestContext context) {
        do {
            executeActions(context);
            index++;
        } while (!checkCondition(context));
    }
}
