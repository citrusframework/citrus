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
 * Sequence of Citrus test actions that get executed after a test suite run. Sequence should
 * decide weather to execute according to given suite name and included test groups if any.
 *
 * @author Christoph Deppisch
 */
public class SequenceAfterSuite extends AbstractSuiteActionContainer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SequenceAfterSuite.class);
    
    @Autowired
    private TestSuiteListeners testSuiteListener = new TestSuiteListeners();
    
    @Override
    public void doExecute(TestContext context) {
        boolean success = true;

        testSuiteListener.onFinish();

        log.info("Executing " + actions.size() + " actions after suite");
        log.info("");

        for(TestAction action: actions)  {
            try {
                /* Executing test action and validate its success */
                action.execute(context);
            } catch (Exception e) {
                log.error("After suite action failed " + action.getName()
                        + "Nested exception is: ", e);
                log.error("Continue after suite actions");
                success = false;
            }
        }

        if (success) {
            testSuiteListener.onFinishSuccess();
        } else {
            testSuiteListener.onFinishFailure(new CitrusRuntimeException("Error in after suite"));
            throw new CitrusRuntimeException("Error in after suite");
        }
    }
    
    /**
     * Sets the testSuiteListener.
     * @param testSuiteListener the testSuiteListener to set
     */
    public void setTestSuiteListener(TestSuiteListeners testSuiteListener) {
        this.testSuiteListener = testSuiteListener;
    }
}
