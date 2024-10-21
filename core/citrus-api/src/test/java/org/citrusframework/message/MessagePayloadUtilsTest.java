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

package org.citrusframework.message;

import static org.testng.Assert.assertEquals;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MessagePayloadUtilsTest {

    @Test
    public void shouldPrettyPrintJson() {
        assertEquals(MessagePayloadUtils.prettyPrint(""), "");
        assertEquals(MessagePayloadUtils.prettyPrint("{}"), "{}");
        assertEquals(MessagePayloadUtils.prettyPrint("[]"), "[]");
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\"}"),
                String.format("{%n  \"user\": \"citrus\"%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"text\":\"<?;,{}' '[]:>\"}"),
                String.format("{%n  \"text\": \"<?;,{}' '[]:>\"%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint(String.format("%n%n  {  \"user\":%n%n \"citrus\"  }")),
                String.format("{%n  \"user\": \"citrus\"%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("[22, 32]"),
                String.format("[%n22,%n32%n]"));
        assertEquals(MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}]"),
                String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  }%n]"));
        assertEquals(MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}, {\"user\":\"foo\",\"age\": 99}]"),
                String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  },%n  {%n    \"user\": \"foo\",%n    \"age\": 99%n  }%n]"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pet\":{\"name\": \"fluffy\", \"age\": 4}}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pet\": {%n    \"name\": \"fluffy\",%n    \"age\": 4%n  }%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pets\":[\"fluffy\",\"hasso\"]}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ]%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[\"fluffy\",\"hasso\"],\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ],%n  \"age\": 32%n}"));
        assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[{\"name\": \"fluffy\", \"age\": 4},{\"name\": \"hasso\", \"age\": 2}],\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    {%n      \"name\": \"fluffy\",%n      \"age\": 4%n    },%n    {%n      \"name\": \"hasso\",%n      \"age\": 2%n    }%n  ],%n  \"age\": 32%n}"));
    }

    @Test
    public void shouldPrettyPrintXml() {
        assertEquals(MessagePayloadUtils.prettyPrint(""), "");
        assertEquals(MessagePayloadUtils.prettyPrint("<root></root>"),
                String.format("<root>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint("<root><text>Citrus rocks!</text></root>"),
                String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><text>Citrus rocks!</text></root>"),
                String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root>%n<text>%nCitrus rocks!%n</text>%n</root>")),
                String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root>%n  <text language=\"eng\">%nCitrus rocks!%n  </text>%n</root>")),
                String.format("<root>%n  <text language=\"eng\">Citrus rocks!</text>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint(String.format("%n%n  <root><text language=\"eng\"><![CDATA[Citrus rocks!]]></text></root>")),
                String.format("<root>%n  <text language=\"eng\">%n    <![CDATA[Citrus rocks!]]>%n  </text>%n</root>%n"));
        assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root><text language=\"eng\" important=\"true\"><![CDATA[%n  Citrus rocks!%n  ]]></text></root>")),
                String.format("<root>%n  <text language=\"eng\" important=\"true\">%n    <![CDATA[%n  Citrus rocks!%n  ]]>%n  </text>%n</root>%n"));
    }

    @DataProvider
    public Object[][] normalizeWhitespaceProvider() {
        return new Object[][] {
            // Test data: payload, ignoreWhitespace, ignoreNewLineType, expected result
            {"Hello    \t\r\nWorld\r\n", true, true, "Hello World"},
            {"Hello    \t\r\nWorld\r\n", true, false, "Hello World"},
            {"Hello    \t\r\nWorld\r\n", false, true, "Hello    \t\nWorld\n"},
            {"Hello    \t\r\nWorld\r\n", false, false, "Hello    \t\r\nWorld\r\n"},
            {"", true, true, ""},
            {"", false, false, ""},
            {null, true, true, null},
            {null, false, false, null}
        };
    }

    @Test(dataProvider = "normalizeWhitespaceProvider")
    public void normalizeWhitespace(String text, boolean normalizeWhitespace, boolean normalizeLineEndingsToUnix, String expected) {
        assertEquals(MessagePayloadUtils.normalizeWhitespace(text, normalizeWhitespace, normalizeLineEndingsToUnix), expected);
    }
}
