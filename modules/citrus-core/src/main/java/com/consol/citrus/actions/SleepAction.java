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

package com.consol.citrus.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.variable.VariableUtils;

/**
 * Stop the test execution for a given amount of time.
 * 
 * @author Christoph Deppisch
 * @since 2006
 */
public class SleepAction extends AbstractTestAction {
    /** Delay time in seconds */
    private String delay;

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SleepAction.class);

    /**
     * @see com.consol.citrus.TestAction#execute(TestContext)
     * @throws CitrusRuntimeException
     */
    @Override
    public void execute(TestContext context) {
        String value = null;

        if (VariableUtils.isVariableName(delay)) {
            value = context.getVariable(delay);
        } else if(context.getFunctionRegistry().isFunction(delay)) {
            value = FunctionUtils.resolveFunction(delay, context);
        } else {
            value = delay;
        }

        try {
            log.info("Sleeping " + value + " seconds");

            Thread.sleep((long)(new Double(value).doubleValue() * 1000));

            log.info("Returning after " + value + " seconds");
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Setter for delay
     * @param delay
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }
}
