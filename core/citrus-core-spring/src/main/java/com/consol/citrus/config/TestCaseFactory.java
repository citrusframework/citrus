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

package com.consol.citrus.config;

import java.util.List;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.container.AfterTest;
import com.consol.citrus.container.BeforeTest;
import com.consol.citrus.report.TestActionListeners;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test case factory bean constructs test cases with test actions and test finally block.
 *
 * @author Christoph Deppisch
 */
public class TestCaseFactory implements FactoryBean<TestCase> {
    /** Result test case object */
    private TestCase testCase;

    /** Test action chain */
    private List<TestAction> testActions;
    /** Test actions in finally block */
    private List<TestAction> finalActions;

    @Autowired
    private TestActionListeners testActionListeners;

    @Autowired(required = false)
    private List<BeforeTest> beforeTest;

    @Autowired(required = false)
    private List<AfterTest> afterTest;

    @Override
    public TestCase getObject() throws Exception {
        if (this.testActions != null && this.testActions.size() > 0) {
            for (TestAction action : testActions) {
                testCase.addTestAction(action);
            }
        }

        if (this.finalActions != null && this.finalActions.size() > 0) {
            for (TestAction action : finalActions) {
                testCase.addFinalAction(action);
            }
        }

        testCase.setTestActionListeners(testActionListeners);
        testCase.setBeforeTest(beforeTest);
        testCase.setAfterTest(afterTest);

        return this.testCase;
    }

    @Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public Class getObjectType() {
        return TestCase.class;
    }

	@Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Setter for final test actions.
     * @param finalActions
     */
    public void setFinalActions(List<TestAction> finalActions) {
        this.finalActions = finalActions;
    }

    /**
     * Set the test case object.
     * @param testCase
     */
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Set the test actions.
     * @param testActions
     */
    public void setTestActions(List<TestAction> testActions) {
        this.testActions = testActions;
    }
}
