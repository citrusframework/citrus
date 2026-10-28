/*
 * Copyright the original author or authors.
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

package org.citrusframework.base.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.CitrusSettings;
import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestCase;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.actions.CreateVariablesAction;
import org.citrusframework.api.container.StopTimer;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.VariableNullValueException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.report.MessageListeners;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.SegmentVariableExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TestContextTest extends UnitTestSupport {

    private final GlobalVariables globalVariables = new GlobalVariables();

    @Test
    public void testDefaultVariables() {
        globalVariables.getVariables().put("defaultVar", "123");

        DefaultTestCase testcase = new DefaultTestCase();
        testcase.setName("TestContextTest");

        testcase.setVariableDefinitions(Collections.singletonMap("test1Var", "456"));

        TestContext testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase.execute(testContext);

        Assertions.assertEquals("TestContextTest", testContext.getVariables().get(CitrusSettings.getTestNameVariable()));
        Assertions.assertEquals(DefaultTestCase.class.getPackage().getName(), testContext.getVariables().get(CitrusSettings.getTestPackageVariable()));
        Assertions.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assertions.assertEquals("123", testContext.getVariables().get("defaultVar"));
        Assertions.assertTrue(testContext.getVariables().containsKey("test1Var"));
        Assertions.assertEquals("456", testContext.getVariables().get("test1Var"));

        DefaultTestCase testcase2 = new DefaultTestCase();
        testcase2.setName("TestContextTest2");
        testcase2.setPackageName("org.citrusframework");

        testcase2.setVariableDefinitions(Collections.singletonMap("test2Var", "456"));

        testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase2.execute(testContext);

        Assertions.assertEquals("TestContextTest2", testContext.getVariables().get(CitrusSettings.getTestNameVariable()));
        Assertions.assertEquals(TestCase.class.getPackage().getName(), testContext.getVariables().get(CitrusSettings.getTestPackageVariable()));
        Assertions.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assertions.assertEquals("123", testContext.getVariables().get("defaultVar"));
        Assertions.assertTrue(testContext.getVariables().containsKey("test2Var"));
        Assertions.assertEquals("456", testContext.getVariables().get("test2Var"));
        Assertions.assertFalse(testContext.getVariables().containsKey("test1Var"));
    }

    @Test
    public void testDefaultVariablesChange() {
        globalVariables.getVariables().put("defaultVar", "123");

        TestCase testcase = new DefaultTestCase();
        testcase.setName("TestContextTest");

        CreateVariablesAction varSetting = new CreateVariablesAction.Builder()
                .variable("defaultVar", "ABC")
                .build();
        testcase.addTestAction(varSetting);

        TestContext testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase.execute(testContext);

        Assertions.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assertions.assertEquals("ABC", testContext.getVariables().get("defaultVar"));

        TestCase testcase2 = new DefaultTestCase();
        testcase2.setName("TestContextTest2");

        testContext = testContextFactory.getObject();
        testContext.setGlobalVariables(globalVariables);
        testcase2.execute(testContext);

        Assertions.assertTrue(testContext.getVariables().containsKey("defaultVar"));
        Assertions.assertEquals("123", testContext.getVariables().get("defaultVar"));
    }

    @Test
    public void testGetVariable() {
        context.getVariables().put("test", "123");

        Assertions.assertEquals("123", context.getVariable("${test}"));
        Assertions.assertEquals("123", context.getVariable("test"));
    }

    @Test
    public void testUnknownVariable() {
        context.getVariables().put("test", "123");

        assertThrows(CitrusRuntimeException.class, () -> context.getVariable("${test_wrong}"));
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

        Assertions.assertEquals(DataContainer.class.getName(), context.getVariable("${helloData}"));
        Assertions.assertEquals("hello", context.getVariable("${helloData.data}"));
        Assertions.assertEquals("99", context.getVariable("${helloData.number}"));
        Assertions.assertEquals("FOO", context.getVariable("${helloData.CONSTANT}"));
        Assertions.assertEquals(DataContainer.class.getName(), context.getVariable("${container.data}"));
        Assertions.assertEquals("nested", context.getVariable("${container.data.data}"));
        Assertions.assertEquals("99", context.getVariable("${container.data.number}"));
        Assertions.assertEquals("FOO", context.getVariable("${container.data.CONSTANT}"));
        Assertions.assertEquals("1", context.getVariable("${container.intVals[1]}"));
        Assertions.assertEquals("B", context.getVariable("${containerArray[3].data[1].data}"));
     }

    @Test
    public void testGetVariableFromListPathExpression() {
        context.setVariable("listContainer", new DataContainer("hello"));
        context.setVariable("manyArr", new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});

        Assertions.assertEquals("B", context.getVariable("${listContainer.strVals[1]}"));
        Assertions.assertEquals("1", context.getVariable("${listContainer.intList[0]}"));
        Assertions.assertEquals("Y", context.getVariable("${listContainer.items[1].code}"));
        Assertions.assertEquals("v12", context.getVariable("${listContainer.manyVals[12]}"));
        Assertions.assertEquals("12", context.getVariable("${manyArr[12]}"));
        Assertions.assertThrows(IndexOutOfBoundsException.class, () -> context.getVariable("${listContainer.strVals[9]}"));

        context.setVariable("mapVar", Map.of("list", List.of("A", "B")));
        Assertions.assertEquals("A", context.getVariable("${mapVar.list[0]}"));

        Assertions.assertThrows(CitrusRuntimeException.class, () -> context.getVariable("${listContainer.nullList[0]}"));
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

        Assertions.assertEquals("Peter", context.getVariable(String.format("${%s}", variableExpression)));
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

        Assertions.assertThrows(Throwable.class, () -> context.getVariable(String.format("${%s}", variableExpression)));
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

        Assertions.assertEquals("Peter", context.getVariable(String.format("${%s}", variableExpression)));
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

        Assertions.assertThrows(Throwable.class, () -> context.getVariable(String.format("${%s}", variableExpression)));
    }

    @Test
    public void testUnknownFromPathExpression() {
        context.setVariable("helloData", new DataContainer("hello"));
        context.setVariable("container", new DataContainer(new DataContainer("nested")));

        Assertions.assertThrows(Throwable.class, () ->context.getVariable("${helloData.unknown}")) ;
        Assertions.assertThrows(Throwable.class, () ->context.getVariable("${container.data.unknown}")) ;
        Assertions.assertThrows(Throwable.class, () ->context.getVariable("${something.else}")) ;
        Assertions.assertThrows(Throwable.class, () ->context.getVariable("${helloData[1]}")) ;

    }

    @Test
    public void testReplaceDynamicContentInString() {
        context.getVariables().put("test", "456");

        Assertions.assertEquals("Variable test is: 456", context.replaceDynamicContentInString("Variable test is: ${test}"));
        Assertions.assertEquals("456 is the value of variable test", context.replaceDynamicContentInString("${test} is the value of variable test"));
        Assertions.assertEquals("123456789", context.replaceDynamicContentInString("123${test}789"));

        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("Hello TestFramework!"));
        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')"));
        Assertions.assertEquals("citrus:citrus", context.replaceDynamicContentInString("citrus:concat('citrus', ':citrus')"));
        Assertions.assertEquals("citrus:citrus", context.replaceDynamicContentInString("citrus:concat('citrus:citrus')"));

        Assertions.assertEquals("Variable test is: '456'", context.replaceDynamicContentInString("Variable test is: ${test}", true));
        Assertions.assertEquals("'456' is the value of variable test", context.replaceDynamicContentInString("${test} is the value of variable test", true));
        Assertions.assertEquals("123'456'789", context.replaceDynamicContentInString("123${test}789", true));

        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("Hello TestFramework!", true));
        Assertions.assertEquals("'Hello TestFramework!'", context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')", true));

        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("Hello TestFramework!"));
        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')"));

        Assertions.assertEquals("Hello TestFramework!", context.replaceDynamicContentInString("Hello TestFramework!", true));
        Assertions.assertEquals("'Hello TestFramework!'", context.replaceDynamicContentInString("citrus:concat('Hello', ' TestFramework!')", true));

        Assertions.assertEquals("123 456789", context.replaceDynamicContentInString("123 ${test}789"));
        Assertions.assertEquals("123 '456'789", context.replaceDynamicContentInString("123 ${test}789", true));
    }

    @Test
    public void testVariableExpressionEscaped() {
        Assertions.assertEquals("${escaped}", context.replaceDynamicContentInString("${//escaped//}"));
        Assertions.assertEquals("${escaped} That is ok!", context.replaceDynamicContentInString("citrus:concat('${////escaped////}', ' That is ok!')"));

        context.setVariable("/value/", "123");
        context.setVariable("value", "456");
        Assertions.assertEquals("123", context.replaceDynamicContentInString("${/value/}"));
        Assertions.assertEquals("${value}", context.replaceDynamicContentInString("${//value//}"));
    }

    @Test
    public void testSetVariable() {
        context.setVariable("${test1}", "123");
        context.setVariable("${test2}", "");

        Assertions.assertEquals("123", context.getVariable("test1"));
        Assertions.assertEquals("", context.getVariable("test2"));
    }

    @Test
    public void testFailSetVariableNoName() {
        assertThrows(CitrusRuntimeException.class, () -> context.setVariable("", "123"));
    }

    @Test
    public void testFailSetVariableNoValue() {
        assertThrows(VariableNullValueException.class, () -> context.setVariable("${test}", null));
    }

    @Test
    public void testAddVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("${test1}", "123");
        vars.put("${test2}", "");

        context.addVariables(vars);

        Assertions.assertEquals("123", context.getVariable("test1"));
        Assertions.assertEquals("", context.getVariable("test2"));
    }

    @Test
    public void testAddVariablesFromArrays() {

        //GIVEN
        String[] variableNames = {"variable1", "${variable2}"};
        Object[] variableValues= {"value1", ""};

        //WHEN
        context.addVariables(variableNames, variableValues);

        //THEN
        Assertions.assertEquals("value1", context.getVariable("variable1"));
        Assertions.assertEquals("", context.getVariable("variable2"));
    }

    @Test
    public void testAddVariablesThrowsExceptionIfArraysHaveDifferentSize() {

        //GIVEN
        String[] variableNames = {"variable1", "variable2"};
        Object[] variableValues= {"value1"};

        //WHEN
        assertThrows(CitrusRuntimeException.class, () -> context.addVariables(variableNames, variableValues));

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

        Assertions.assertEquals("123", testMap.get("value"));

        testMap.clear();
        testMap.put("value", "test");

        testMap = context.resolveDynamicValuesInMap(testMap);

        Assertions.assertEquals("test", testMap.get("value"));

        testMap.clear();
        testMap.put("${test}", "value");

        testMap = context.resolveDynamicValuesInMap(testMap);

        // Should be null due to variable substitution
        Assertions.assertNull(testMap.get("${test}"));
        // Should return "test" after variable substitution
        Assertions.assertEquals("value", testMap.get("123"));
    }

    @Test
    public void testReplaceVariablesInList() {
        context.getVariables().put("test", "123");

        List<String> testList = new ArrayList<>();
        testList.add("Hello TestFramework!");
        testList.add("${test}");
        testList.add("test");

        List<String> replaceValues = context.resolveDynamicValuesInList(testList);

        Assertions.assertEquals("Hello TestFramework!", replaceValues.get(0));
        Assertions.assertEquals("123", replaceValues.get(1));
        Assertions.assertEquals("test", replaceValues.get(2));
    }

    @Test
    public void testReplaceVariablesInArray() {
        context.getVariables().put("test", "123");

        String[] testArray = new String[] { "Hello TestFramework!", "${test}", "test" };

        String[] replaceValues = context.resolveDynamicValuesInArray(testArray);

        Assertions.assertEquals("Hello TestFramework!", replaceValues[0]);
        Assertions.assertEquals("123", replaceValues[1]);
        Assertions.assertEquals("test", replaceValues[2]);
    }

    @Test
    public void testResolveDynamicValue() {
        context.getVariables().put("test", "testtesttest");

        Assertions.assertEquals("testtesttest", context.resolveDynamicValue("${test}"));
        Assertions.assertEquals("Hello TestFramework!", context.resolveDynamicValue(
                "citrus:concat('Hello', ' TestFramework!')"));
        Assertions.assertEquals("nonDynamicValue", context.resolveDynamicValue("nonDynamicValue"));
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

        Assertions.assertTrue(context.stopTimer(timerId));
        Assertions.assertFalse(context.stopTimer("?????"));
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

        private final List<String> strVals = List.of("A", "B", "C");

        private final List<Integer> intList = List.of(1, 2, 3);

        private final List<Item> items = List.of(new Item("X"), new Item("Y"));

        private final List<String> manyVals = List.of("v0", "v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8", "v9", "v10", "v11", "v12");

        private final List<String> nullList = null;

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

    /**
     * Simple holder for list element field traversal.
     */
    private static class Item {
        private final String code;

        Item(String code) {
            this.code = code;
        }
    }
}
