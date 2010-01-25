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
                testCase.addTestChainAction(action);
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
