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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;

/**
 * Sequence container executing a set of nested test actions in simple sequence. 
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class Sequence extends AbstractActionContainer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Sequence.class);

    /**
     * Default constructor.
     */
    public Sequence() {
        setName("sequential");
    }

    @Override
    public void doExecute(TestContext context) {
        for (TestAction action: actions) {
            setActiveAction(action);
            action.execute(context);
        }

        log.debug("Action sequence finished successfully");
    }
}
