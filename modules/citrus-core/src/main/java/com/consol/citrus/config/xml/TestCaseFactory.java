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

package com.consol.citrus.config.xml;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;

/**
 * Test case factory bean constructs test cases with action chain and finally block.
 * 
 * @author Christoph Deppisch
 */
public class TestCaseFactory implements FactoryBean {
    /** Result test case object */
    private TestCase testCase;

    /** Test action chain */
    private List<TestAction> testChain;
    /** Test actions in finally block */
    private List<TestAction> finallyChain;

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        if (this.testChain != null && this.testChain.size() > 0) {
            for (int i = 0; i < testChain.size(); i++) {
                TestAction action = testChain.get(i);
                testCase.addTestAction(action);
            }
        }

        if (this.finallyChain != null && this.finallyChain.size() > 0) {
            for (int i = 0; i < finallyChain.size(); i++) {
                TestAction action = finallyChain.get(i);
                testCase.addFinallyChainAction(action);
            }
        }

        return this.testCase;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
	@SuppressWarnings("unchecked")
    public Class getObjectType() {
        return TestCase.class;
    }

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Setter for finally chain.
     * @param finallyChain
     */
    public void setFinallyChain(List<TestAction> finallyChain) {
        this.finallyChain = finallyChain;
    }

    /**
     * Set the test case object.
     * @param testCase
     */
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Set the test action chain.
     * @param testChain
     */
    public void setTestChain(List<TestAction> testChain) {
        this.testChain = testChain;
    }
}
