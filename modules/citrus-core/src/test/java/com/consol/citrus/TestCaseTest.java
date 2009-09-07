package com.consol.citrus;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;

public class TestCaseTest extends AbstractBaseTest {
    
    @Test
    public void testExecution() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoAction());
        
        testcase.execute();
    }
    
    @Test
    public void testExecutionWithVariables() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${text}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testUnknownVariable() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        final String message = "Hello TestFramework!";
        testcase.setVariableDefinitions(Collections.singletonMap("text", message));
        
        testcase.addTestChainAction(new AbstractTestAction() {
            @Override
            public void execute(TestContext context) {
                Assert.assertEquals(context.getVariable("${unknown}"), message);
            }
        });
        
        testcase.execute();
    }
    
    @Test
    public void testFinallyChain() {
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        testcase.setTestContext(context);
        
        testcase.addTestChainAction(new EchoAction());
        testcase.addFinallyChainAction(new EchoAction());
        
        testcase.execute();
        testcase.finish();
    }
}
