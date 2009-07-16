package com.consol.citrus;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.actions.EchoBean;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestSuiteException;

public class TestCaseTest extends AbstractBaseTest {
    
    @Test
    public void testExecution() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoBean());
        
        testcase.execute();
    }
    
    @Test
    public void testExecutionWithVariables() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) throws TestSuiteException {
                Assert.assertEquals(context.getVariable("${text}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test(expectedExceptions = {TestSuiteException.class})
    public void testUnknownVariable() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) throws TestSuiteException {
                Assert.assertEquals(context.getVariable("${unknown}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test
    public void testFinallyChain() {
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoBean());
        testcase.addFinallyChainAction(new EchoBean());
        
        testcase.execute();
        testcase.finish();
    }
}
