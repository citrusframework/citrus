package com.consol.citrus.schema;

import java.util.List;

import org.springframework.beans.factory.FactoryBean;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;


public class TestCaseFactory implements FactoryBean {
    private TestCase testCase;
    private List testChain;
    private List finallyChain;

    public Object getObject() throws Exception {
        if (this.testChain != null && this.testChain.size() > 0) {
            for (int i = 0; i < testChain.size(); i++) {
                TestAction action = (TestAction)testChain.get(i);
                testCase.addTestChainAction(action);
            }
        }

        if (this.finallyChain != null && this.finallyChain.size() > 0) {
            for (int i = 0; i < finallyChain.size(); i++) {
                TestAction action = (TestAction)finallyChain.get(i);
                testCase.addFinallyChainAction(action);
            }
        }

        return this.testCase;
    }

    public Class getObjectType() {
        return TestCase.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setFinallyChain(List finallyChain) {
        this.finallyChain = finallyChain;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public void setTestChain(List testChain) {
        this.testChain = testChain;
    }
}
