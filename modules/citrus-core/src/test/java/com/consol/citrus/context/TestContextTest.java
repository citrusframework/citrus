package com.consol.citrus.context;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.SetVariablesBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.VariableNullValueException;
import com.consol.citrus.variable.GlobalVariables;

public class TestContextTest extends AbstractBaseTest {

    @Autowired
    GlobalVariables globalVariables;
    
    @Test
    public void testDefaultVariables() {
        globalVariables.getVariables().put("defaultVar", "123");
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        
        testcase.setVariableDefinitions(Collections.singletonMap("test1Var", "456"));
        
        testcase.execute();
        
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("test1Var"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("test1Var"), "456");
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("MyTestCase2");
        
        testcase2.setVariableDefinitions(Collections.singletonMap("test2Var", "456"));
        
        testcase2.execute();
        
        Assert.assertTrue(testcase2.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase2.getTestContext().getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testcase2.getTestContext().getVariables().containsKey("test2Var"));
        Assert.assertEquals(testcase2.getTestContext().getVariables().get("test2Var"), "456");
        Assert.assertFalse(testcase2.getTestContext().getVariables().containsKey("test1Var"));
    }
    
    @Test
    public void testDefaultVariablesChange() {
        globalVariables.getVariables().put("defaultVar", "123");
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("MyTestCase");
        
        SetVariablesBean varSetting = new SetVariablesBean();
        varSetting.setVariables(Collections.singletonMap("defaultVar", "ABC"));
        testcase.addTestChainAction(varSetting);
        testcase.execute();
        
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("defaultVar"), "ABC");
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("MyTestCase2");
        
        testcase2.execute();
        
        Assert.assertTrue(testcase2.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase2.getTestContext().getVariables().get("defaultVar"), "123");
    }
    
    @Test
    public void testGetVariable() {
        context.getVariables().put("test", "123");
        
        Assert.assertEquals(context.getVariable("${test}"), "123");
        Assert.assertEquals(context.getVariable("test"), "123");
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testUnknownVariable() {
        context.getVariables().put("test", "123");
        
        context.getVariable("${test_wrong}");
    }
    
    @Test
    public void testReplaceDynamicContentInString() throws java.text.ParseException {
        context.getVariables().put("test", "456");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Variable test is: ${test}"), "Variable test is: 456");
        Assert.assertEquals(context.replaceDynamicContentInString("${test} is the value of variable test"), "456 is the value of variable test");
        Assert.assertEquals(context.replaceDynamicContentInString("123${test}789"), "123456789");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Hello TestFramework!"), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')"), "Hello TestFramework!");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Variable test is: ${test}", true), "Variable test is: '456'");
        Assert.assertEquals(context.replaceDynamicContentInString("${test} is the value of variable test", true), "'456' is the value of variable test");
        Assert.assertEquals(context.replaceDynamicContentInString("123${test}789", true), "123'456'789");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Hello TestFramework!", true), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')", true), "'Hello TestFramework!'");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Hello TestFramework!"), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')"), "Hello TestFramework!");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Hello TestFramework!", true), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')", true), "'Hello TestFramework!'");
        
        Assert.assertEquals(context.replaceDynamicContentInString("123 ${test}789"), "123 456789");
        Assert.assertEquals(context.replaceDynamicContentInString("123 ${test}789", true), "123 '456'789");
    }
    
    @Test
    public void testSetVariable() {
        context.setVariable("${test1}", "123");
        context.setVariable("${test2}", "");
        
        Assert.assertEquals(context.getVariable("test1"), "123");
        Assert.assertEquals(context.getVariable("test2"), "");
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testFailSetVariableNoName() {
        context.setVariable("", "123");
    }
    
    @Test(expectedExceptions = {VariableNullValueException.class})
    public void testFailSetVariableNoValue() {
        context.setVariable("${test}", null);
    }
    
    @Test
    public void testAddVariables() {
        Map vars = new HashMap();
        vars.put("${test1}", "123");
        vars.put("${test2}", "");
        
        context.addVariables(vars);
        
        Assert.assertEquals(context.getVariable("test1"), "123");
        Assert.assertEquals(context.getVariable("test2"), "");
    }
    
    @Test
    public void testReplaceVariablesInMap() {
        context.getVariables().put("test", "123");
        
        Map testMap = new HashMap();
        testMap.put("plainText", "Hello TestFramework!");
        testMap.put("value", "${test}");
        
        testMap = context.replaceVariablesInMap(testMap);
        
        Assert.assertEquals(testMap.get("value"), "123");
        
        testMap.clear();
        testMap.put("value", "test");
        
        testMap = context.replaceVariablesInMap(testMap);
        
        Assert.assertEquals(testMap.get("value"), "test");
        
        testMap.clear();
        testMap.put("${value}", "test");
        
        testMap = context.replaceVariablesInMap(testMap);
        
        Assert.assertEquals(testMap.get("${value}"), "test");
    }
    
    @Test
    public void testReplaceVariablesInList() {
        context.getVariables().put("test", "123");
        
        List testList = new ArrayList();
        testList.add("Hello TestFramework!");
        testList.add("${test}");
        testList.add("test");
        
        List replaceValues = context.replaceVariablesInList(testList);
        
        Assert.assertEquals(replaceValues.get(0), "Hello TestFramework!");
        Assert.assertEquals(replaceValues.get(1), "123");
        Assert.assertEquals(replaceValues.get(2), "test");
    }
}
