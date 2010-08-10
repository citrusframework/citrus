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

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.CreateVariablesAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.VariableNullValueException;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.variable.GlobalVariables;

/**
 * @author Christoph Deppisch
 */
public class TestContextTest extends AbstractBaseTest {

    @Autowired
    GlobalVariables globalVariables;
    
    @Test
    public void testDefaultVariables() {
        globalVariables.getVariables().put("defaultVar", "123");
        
        TestCase testcase = new TestCase();
        testcase.setName("MyTestCase");
        
        testcase.setVariableDefinitions(Collections.singletonMap("test1Var", "456"));
        
        testcase.execute(createTestContext());
        
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("test1Var"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("test1Var"), "456");
        
        TestCase testcase2 = new TestCase();
        testcase2.setName("MyTestCase2");
        
        testcase2.setVariableDefinitions(Collections.singletonMap("test2Var", "456"));
        
        testcase2.execute(createTestContext());
        
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
        testcase.setName("MyTestCase");
        
        CreateVariablesAction varSetting = new CreateVariablesAction();
        varSetting.setVariables(Collections.singletonMap("defaultVar", "ABC"));
        testcase.addTestAction(varSetting);
        testcase.execute(createTestContext());
        
        Assert.assertTrue(testcase.getTestContext().getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testcase.getTestContext().getVariables().get("defaultVar"), "ABC");
        
        TestCase testcase2 = new TestCase();
        testcase2.setName("MyTestCase2");
        
        testcase2.execute(createTestContext());
        
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
        Map<String, String> vars = new HashMap<String, String>();
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
        
        List<String> testList = new ArrayList<String>();
        testList.add("Hello TestFramework!");
        testList.add("${test}");
        testList.add("test");
        
        List<String> replaceValues = context.replaceVariablesInList(testList);
        
        Assert.assertEquals(replaceValues.get(0), "Hello TestFramework!");
        Assert.assertEquals(replaceValues.get(1), "123");
        Assert.assertEquals(replaceValues.get(2), "test");
    }
    
    @Test
    public void testReplaceMessageValuesWithXPath() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/TestMessage/Text", "Hello!");
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(context.replaceMessageValues(xPathExpressions, messagePayload))
                .endsWith("<TestMessage><Text>Hello!</Text></TestMessage>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndDefaultNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://www.citrusframework.org/test\">" +
        		"<Text>Hello World!</Text>" +
        		"</TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/:TestMessage/:Text", "Hello!");
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(context.replaceMessageValues(xPathExpressions, messagePayload))
                .contains("<Text>Hello!</Text>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
        		"<ns0:Text>Hello World!</ns0:Text>" +
        		"</ns0:TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns0:Text", "Hello!");
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(context.replaceMessageValues(xPathExpressions, messagePayload))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndNestedNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
        		"<ns1:Text xmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello World!</ns1:Text>" +
        		"</ns0:TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns1:Text", "Hello!");
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(context.replaceMessageValues(xPathExpressions, messagePayload))
                .contains("<ns1:Textxmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello!</ns1:Text>"));
    }
}
