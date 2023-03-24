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

package org.citrusframework.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.container.StopTimer;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.VariableNullValueException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.VariableExpressionSegmentMatcher;
import org.citrusframework.variable.SegmentVariableExtractor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.fail;

/**
 * @author Christoph Deppisch
 */
public class TestContextTest extends UnitTestSupport {

    private final GlobalVariables globalVariables = new GlobalVariables();

    @Test
    public void testDefaultVariables() {
        globalVariables.getVariables().put("defaultVar", "123");

        DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        testcase.setVariableDefinitions(Collections.singletonMap("test1Var", "456"));

        TestContext testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase.execute(testContext);

        Assert.assertEquals(testContext.getVariables().get(CitrusSettings.TEST_NAME_VARIABLE), "MyTestCase");
        Assert.assertEquals(testContext.getVariables().get(CitrusSettings.TEST_PACKAGE_VARIABLE), TestCase.class.getPackage().getName());
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testContext.getVariables().containsKey("test1Var"));
        Assert.assertEquals(testContext.getVariables().get("test1Var"), "456");

        DefaultTestCase testcase2 = new DefaultTestCase();
        testcase2.setName("MyTestCase2");
        testcase2.setPackageName("org.citrusframework");

        testcase2.setVariableDefinitions(Collections.singletonMap("test2Var", "456"));

        testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase2.execute(testContext);

        Assert.assertEquals(testContext.getVariables().get(CitrusSettings.TEST_NAME_VARIABLE), "MyTestCase2");
        Assert.assertEquals(testContext.getVariables().get(CitrusSettings.TEST_PACKAGE_VARIABLE), "org.citrusframework");
        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "123");
        Assert.assertTrue(testContext.getVariables().containsKey("test2Var"));
        Assert.assertEquals(testContext.getVariables().get("test2Var"), "456");
        Assert.assertFalse(testContext.getVariables().containsKey("test1Var"));
    }

    @Test
    public void testDefaultVariablesChange() {
        globalVariables.getVariables().put("defaultVar", "123");

        TestCase testcase = new DefaultTestCase();
        testcase.setName("MyTestCase");

        CreateVariablesAction varSetting = new CreateVariablesAction.Builder()
                .variable("defaultVar", "ABC")
                .build();
        testcase.addTestAction(varSetting);

        TestContext testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase.execute(testContext);

        Assert.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assert.assertEquals(testContext.getVariables().get("defaultVar"), "ABC");

        TestCase testcase2 = new DefaultTestCase();
        testcase2.setName("MyTestCase2");

        testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
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
    public void testGetVariableFromPathExpression() {
        context.setVariable("helloData", new DataContainer("hello"));
        context.setVariable("container", new DataContainer(new DataContainer("nested")));

        DataContainer[] subContainerArray = new DataContainer[] {
                new DataContainer("A"),
                new DataContainer("B"),
                new DataContainer("C"),
                new DataContainer("D"),
        };

        DataContainer[] containerArray = new DataContainer[] {
                new DataContainer("0"),
                new DataContainer("1"),
                new DataContainer("2"),
                new DataContainer(subContainerArray),
        };

        context.setVariable("containerArray", containerArray);

        Assert.assertEquals(context.getVariable("${helloData}"), DataContainer.class.getName());
        Assert.assertEquals(context.getVariable("${helloData.data}"), "hello");
        Assert.assertEquals(context.getVariable("${helloData.number}"), "99");
        Assert.assertEquals(context.getVariable("${helloData.CONSTANT}"), "FOO");
        Assert.assertEquals(context.getVariable("${container.data}"), DataContainer.class.getName());
        Assert.assertEquals(context.getVariable("${container.data.data}"), "nested");
        Assert.assertEquals(context.getVariable("${container.data.number}"), "99");
        Assert.assertEquals(context.getVariable("${container.data.CONSTANT}"), "FOO");
        Assert.assertEquals(context.getVariable("${container.intVals[1]}"), "1");
        Assert.assertEquals(context.getVariable("${containerArray[3].data[1].data}"), "B");
     }

    @Test
    public void testGetVariableFromJsonPathExpression() {
        String json = "{\"name\": \"Peter\"}";
        context.setVariable("jsonVar", json);

        String variableExpression = "jsonVar.jsonPath($.name)";

        SegmentVariableExtractor jsonExtractorMock = Mockito.mock(SegmentVariableExtractor.class);
        context.getSegmentVariableExtractorRegistry().getSegmentValueExtractors().add(jsonExtractorMock);
        
        Mockito.doReturn(true).when(jsonExtractorMock).canExtract(Mockito.eq(context), Mockito.eq(json), Mockito.any());
        Mockito.doReturn("Peter").when(jsonExtractorMock).extractValue(Mockito.eq(context), Mockito.eq(json), Mockito.any());

        Assert.assertEquals(context.getVariable(String.format("${%s}", variableExpression)), "Peter");
    }

    @Test
    public void testGetVariableFromJsonPathExpressionNoMatch() {
        String json = "{\"name\": \"Peter\"}";
        context.setVariable("jsonVar", json);

        String variableExpression = "jsonVar.jsonPath($.othername)";

        SegmentVariableExtractor jsonExtractorMock = Mockito.mock(SegmentVariableExtractor.class);
        context.getSegmentVariableExtractorRegistry().getSegmentValueExtractors().add(jsonExtractorMock);

        Mockito.doReturn(true).when(jsonExtractorMock).canExtract(Mockito.eq(context), Mockito.eq(json), Mockito.any());
        Mockito.doThrow(new CitrusRuntimeException()).when(jsonExtractorMock).extractValue(Mockito.eq(context), Mockito.eq(json), Mockito.any());

        Assert.assertThrows(() -> context.getVariable(String.format("${%s}", variableExpression)));
    }

    @Test
    public void testGetVariableFromXpathExpression() {
        String xml = "<person><name>Peter</name><person>";
        context.setVariable("xpathVar", xml);

        String variableExpression = "xpathVar.xpath(//person/name)";

        SegmentVariableExtractor xpathExtractorMock = Mockito.mock(SegmentVariableExtractor.class);
        context.getSegmentVariableExtractorRegistry().getSegmentValueExtractors().add(xpathExtractorMock);

        Mockito.doReturn(true).when(xpathExtractorMock).canExtract(Mockito.eq(context), Mockito.eq(xml), Mockito.any());
        Mockito.doReturn("Peter").when(xpathExtractorMock).extractValue(Mockito.eq(context), Mockito.eq(xml), Mockito.any());

        Assert.assertEquals(context.getVariable(String.format("${%s}", variableExpression)), "Peter");
    }

    @Test
    public void testGetVariableFromXpathExpressionNoMatch() {
        String xml = "<person><name>Peter</name><person>";
        context.setVariable("xpathVar", xml);

        String variableExpression = "xpathVar.xpath(//person/name)";

        SegmentVariableExtractor xpathExtractorMock = Mockito.mock(SegmentVariableExtractor.class);
        context.getSegmentVariableExtractorRegistry().getSegmentValueExtractors().add(xpathExtractorMock);

        Mockito.doReturn(true).when(xpathExtractorMock).canExtract(Mockito.eq(context), Mockito.eq(xml), Mockito.any());
        Mockito.doThrow(new CitrusRuntimeException()).when(xpathExtractorMock).extractValue(Mockito.eq(context), Mockito.eq(xml), Mockito.any());

        Assert.assertThrows(() -> context.getVariable(String.format("${%s}", variableExpression)));
    }
    
    @Test
    public void testUnknownFromPathExpression() {
        context.setVariable("helloData", new DataContainer("hello"));
        context.setVariable("container", new DataContainer(new DataContainer("nested")));

        Assert.assertThrows(() ->context.getVariable("${helloData.unknown}")) ;
        Assert.assertThrows(() ->context.getVariable("${container.data.unknown}")) ;
        Assert.assertThrows(() ->context.getVariable("${something.else}")) ;
        Assert.assertThrows(() ->context.getVariable("${helloData[1]}")) ;

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
    public void testVariableExpressionEscaped() {
        Assert.assertEquals(context.replaceDynamicContentInString("${//escaped//}"), "${escaped}");
        Assert.assertEquals(context.replaceDynamicContentInString("citrus:concat('${////escaped////}', ' That is ok!')"), "${escaped} That is ok!");

        context.setVariable("/value/", "123");
        context.setVariable("value", "456");
        Assert.assertEquals(context.replaceDynamicContentInString("${/value/}"), "123");
        Assert.assertEquals(context.replaceDynamicContentInString("${//value//}"), "${value}");
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
        Map<String, Object> vars = new HashMap<>();
        vars.put("${test1}", "123");
        vars.put("${test2}", "");

        context.addVariables(vars);

        Assert.assertEquals(context.getVariable("test1"), "123");
        Assert.assertEquals(context.getVariable("test2"), "");
    }

    @Test
    public void testAddVariablesFromArrays() {

        //GIVEN
        String[] variableNames = {"variable1", "${variable2}"};
        Object[] variableValues= {"value1", ""};

        //WHEN
        context.addVariables(variableNames, variableValues);

        //THEN
        Assert.assertEquals(context.getVariable("variable1"), "value1");
        Assert.assertEquals(context.getVariable("variable2"), "");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testAddVariablesThrowsExceptionIfArraysHaveDifferentSize() {

        //GIVEN
        String[] variableNames = {"variable1", "variable2"};
        Object[] variableValues= {"value1"};

        //WHEN
        context.addVariables(variableNames, variableValues);

        //THEN
        //Exception is thrown
    }

    @Test
    public void testReplaceVariablesInMap() {
        context.getVariables().put("test", "123");

        Map<String, Object> testMap = new HashMap<>();
        testMap.put("plainText", "Hello TestFramework!");
        testMap.put("value", "${test}");

        testMap = context.resolveDynamicValuesInMap(testMap);

        Assert.assertEquals(testMap.get("value"), "123");

        testMap.clear();
        testMap.put("value", "test");

        testMap = context.resolveDynamicValuesInMap(testMap);

        Assert.assertEquals(testMap.get("value"), "test");

        testMap.clear();
        testMap.put("${test}", "value");

        testMap = context.resolveDynamicValuesInMap(testMap);

        // Should be null due to variable substitution
        Assert.assertNull(testMap.get("${test}"));
        // Should return "test" after variable substitution
        Assert.assertEquals(testMap.get("123"), "value");
    }

    @Test
    public void testReplaceVariablesInList() {
        context.getVariables().put("test", "123");

        List<String> testList = new ArrayList<>();
        testList.add("Hello TestFramework!");
        testList.add("${test}");
        testList.add("test");

        List<String> replaceValues = context.resolveDynamicValuesInList(testList);

        Assert.assertEquals(replaceValues.get(0), "Hello TestFramework!");
        Assert.assertEquals(replaceValues.get(1), "123");
        Assert.assertEquals(replaceValues.get(2), "test");
    }

    @Test
    public void testReplaceVariablesInArray() {
        context.getVariables().put("test", "123");

        String[] testArray = new String[] { "Hello TestFramework!", "${test}", "test" };

        String[] replaceValues = context.resolveDynamicValuesInArray(testArray);

        Assert.assertEquals(replaceValues[0], "Hello TestFramework!");
        Assert.assertEquals(replaceValues[1], "123");
        Assert.assertEquals(replaceValues[2], "test");
    }

    @Test
    public void testResolveDynamicValue() {
        context.getVariables().put("test", "testtesttest");

        Assert.assertEquals(context.resolveDynamicValue("${test}"), "testtesttest");
        Assert.assertEquals(context.resolveDynamicValue(
                "citrus:concat('Hello', ' TestFramework!')"), "Hello TestFramework!");
        Assert.assertEquals(context.resolveDynamicValue("nonDynamicValue"), "nonDynamicValue");
    }

    @Test
    public void testRegisterAndStopTimers() {
        String timerId = "t1";
        StopTimer timer = Mockito.mock(StopTimer.class);

        context.registerTimer(timerId, timer);

        try {
            context.registerTimer(timerId, timer);
            fail("registering timer with same name more than once should have thrown exception");
        }
        catch (Exception e) {
            // ok
        }

        Assert.assertTrue(context.stopTimer(timerId));
        Assert.assertFalse(context.stopTimer("?????"));
        context.stopTimers();

        verify(timer, times(2)).stopTimer();
    }

    @Test
    public void shouldCallMessageListeners() {
        MessageListeners listeners = Mockito.mock(MessageListeners.class);
        context.setMessageListeners(listeners);

        Message inbound = new DefaultMessage("INBOUND");
        Message outbound = new DefaultMessage("OUTBOUND");
        context.onInboundMessage(inbound);
        context.onOutboundMessage(outbound);

        verify(listeners).onInboundMessage(same(inbound), eq(context));
        verify(listeners).onOutboundMessage(same(outbound), eq(context));
    }

    /**
     * Data container for test variable object access.
     */
    private static class DataContainer {
        private final int number = 99;
        private final Object data;

        private final int[] intVals =  new int[] {0, 1, 2, 3, 4};
        
        private static final String CONSTANT = "FOO";

        /**
         * Constructor with data.
         * @param data
         */
        public DataContainer(Object data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return DataContainer.class.getName();
        }
    }
}
