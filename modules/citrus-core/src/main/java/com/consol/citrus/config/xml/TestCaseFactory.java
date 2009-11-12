/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;


public class TestCaseFactory implements FactoryBean {
    private TestCase testCase;
    private List<TestAction> testChain;
    private List<TestAction> finallyChain;

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

	@SuppressWarnings("unchecked")
    public Class getObjectType() {
        return TestCase.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setFinallyChain(List<TestAction> finallyChain) {
        this.finallyChain = finallyChain;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public void setTestChain(List<TestAction> testChain) {
        this.testChain = testChain;
    }
}
