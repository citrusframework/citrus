/*
 * Copyright 2006-2011 the original author or authors.
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
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestSuiteListeners;

/**
 * @author Christoph Deppisch
 */
public class SequenceBeforeSuite extends AbstractActionContainer {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SequenceBeforeSuite.class);
    
    @Autowired
    private TestSuiteListeners testSuiteListener = new TestSuiteListeners();
    
    @Autowired(required = false)
    private SequenceAfterSuite afterSuiteActions;
    
    @Override
    public void doExecute(TestContext context) {
        testSuiteListener.onStart();
        
        log.info("Found " + actions.size() + " actions before suite");
        
        for(TestAction action: actions)  {
            try {
                /* Executing test action and validate its success */
                action.execute(context);
            } catch (Exception e) {
                log.error("Task failed "
                        + action.getName()
                        + "Nested exception is: ", e);

                testSuiteListener.onStartFailure(e);

                if (afterSuiteActions != null) {
                    afterSuiteActions.execute(context);
                }
                
                throw new CitrusRuntimeException(e);
            }
        }

        testSuiteListener.onStartSuccess();
    }

    /**
     * Sets the testSuiteListener.
     * @param testSuiteListener the testSuiteListener to set
     */
    public void setTestSuiteListener(TestSuiteListeners testSuiteListener) {
        this.testSuiteListener = testSuiteListener;
    }

    /**
     * Sets the afterSuiteActions.
     * @param afterSuiteActions the afterSuiteActions to set
     */
    public void setAfterSuiteActions(SequenceAfterSuite afterSuiteActions) {
        this.afterSuiteActions = afterSuiteActions;
    }
}
