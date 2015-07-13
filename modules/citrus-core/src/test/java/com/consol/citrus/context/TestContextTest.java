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

package com.consol.citrus.context;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.VariableNullValueException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.variable.GlobalVariables;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Christoph Deppisch
 */
public class TestContextTest extends AbstractTestNGUnitTest {

    @Autowired
    GlobalVariables globalVariables;
    
    @Test
    public void testDefaultVariables() {
        globalVariables.getVariables().put("defaultVar", "123");
        
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        testcase.setVariableDefinitions(Collections.<String, Object>singletonMap("test1Var", "456"));

        TestContext testContext = createTestContext();
        testcase.execute(testContext);

        Assert.assertEquals(testContext.getVariables().get(CitrusConstants.TEST_NAME_VARIABLE), "MyTestCase");
        Assert.assertEquals(testContext.getVariables().get(CitrusConstants.TEST_PACKAGE_VARIABLE), TestCase.class.getPackage().getName());
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testContext.getVariables().containsKey("test1Var"));
        Assert.assertEquals(testContext.getVariables().get("test1Var"), "456");
        
        TestCase testcase2 = new TestCase();
        testcase2.setName("MyTestCase2");
        testcase2.setPackageName("com.consol.citrus");
        
        testcase2.setVariableDefinitions(Collections.<String, Object>singletonMap("test2Var", "456"));

        testContext = createTestContext();
        testcase2.execute(testContext);

        Assert.assertEquals(testContext.getVariables().get(CitrusConstants.TEST_NAME_VARIABLE), "MyTestCase2");
        Assert.assertEquals(testContext.getVariables().get(CitrusConstants.TEST_PACKAGE_VARIABLE), "com.consol.citrus");
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testContext.getVariables().containsKey("test2Var"));
        Assert.assertEquals(testContext.getVariables().get("test2Var"), "456");
        Assert.assertFalse(testContext.getVariables().containsKey("test1Var"));
    }
    
    @Test
    public void testDefaultVariablesChange() {
        globalVariables.getVariables().put("defaultVar", "123");
        
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        CreateVariablesAction varSetting = new CreateVariablesAction();
        varSetting.setVariables(Collections.singletonMap("defaultVar", "ABC"));
        testcase.addTestAction(varSetting);

        TestContext testContext = createTestContext();
        testcase.execute(testContext);
        
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "ABC");
        
        TestCase testcase2 = new TestCase();
        testcase2.setName("MyTestCase2");

        testContext = createTestContext();
        testcase2.execute(testContext);
        
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "123");
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
    public void testReplaceDynamicContentInString() {
        context.getVariables().put("test", "456");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Variable test is: ${test}"), "Variable test is: 456");
        Assert.assertEquals(context.replaceDynamicContentInString("${test} is the value of variable test"), "456 is the value of variable test");
        Assert.assertEquals(context.replaceDynamicContentInString("123${test}789"), "123456789");
        
        Assert.assertEquals(context.replaceDynamicContentInString("Hello TestFramework!"), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')"), "Hello TestFramework!");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('citrus', ':citrus')"), "citrus:citrus");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('citrus:citrus')"), "citrus:citrus");
        
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
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("${test1}", "123");
        vars.put("${test2}", "");
        
        context.addVariables(vars);
        
        Assert.assertEquals(context.getVariable("test1"), "123");
        Assert.assertEquals(context.getVariable("test2"), "");
    }
    
    @Test
    public void testReplaceVariablesInMap() {
        context.getVariables().put("test", "123");
        
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("plainText", "Hello TestFramework!");
        testMap.put("value", "${test}");
        
        testMap = context.resolveDynamicValuesInMap(testMap);
        
        Assert.assertEquals(testMap.get("value"), "123");
        
        testMap.clear();
        testMap.put("value", "test");
        
        testMap = context.resolveDynamicValuesInMap(testMap);
        
        Assert.assertEquals(testMap.get("value"), "test");
        
        testMap.clear();
        testMap.put("${value}", "test");
        
        testMap = context.resolveDynamicValuesInMap(testMap);
        
        Assert.assertEquals(testMap.get("${value}"), "test");
    }
    
    @Test
    public void testReplaceVariablesInList() {
        context.getVariables().put("test", "123");
        
        List<String> testList = new ArrayList<String>();
        testList.add("Hello TestFramework!");
        testList.add("${test}");
        testList.add("test");
        
        List<String> replaceValues = context.resolveDynamicValuesInList(testList);
        
        Assert.assertEquals(replaceValues.get(0), "Hello TestFramework!");
        Assert.assertEquals(replaceValues.get(1), "123");
        Assert.assertEquals(replaceValues.get(2), "test");
    }
    
    @Test
    public void testResolveDynamicValue() {
        context.getVariables().put("test", "testtesttest");

        Assert.assertEquals(context.resolveDynamicValue("${test}"), "testtesttest");
        Assert.assertEquals(context.resolveDynamicValue(
                "citrus:concat('Hello', ' TestFramework!')"), "Hello TestFramework!");
        Assert.assertEquals(context.resolveDynamicValue("nonDynamicValue"), "nonDynamicValue");
    }
}
