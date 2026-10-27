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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessagePayloadUtilsTest {

    @Test
    public void shouldPrettyPrintJson() {
        assertEquals("", MessagePayloadUtils.prettyPrint(""));
        assertEquals("{}", MessagePayloadUtils.prettyPrint("{}"));
        assertEquals("[]", MessagePayloadUtils.prettyPrint("[]"));
        assertEquals(String.format("{%n  \"user\": \"citrus\"%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\"}"));
        assertEquals(String.format("{%n  \"text\": \"<?;,{}' '[]:>\"%n}"),
                MessagePayloadUtils.prettyPrint("{\"text\":\"<?;,{}' '[]:>\"}"));
        assertEquals(String.format("{%n  \"user\": \"citrus\"%n}"),
                MessagePayloadUtils.prettyPrint(String.format("%n%n  {  \"user\":%n%n \"citrus\"  }")));
        assertEquals(String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32}"));
        assertEquals("[22, 32]", MessagePayloadUtils.prettyPrint("[22, 32]"));
        assertEquals(String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  }%n]"),
                MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}]"));
        assertEquals(String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  },%n  {%n    \"user\": \"foo\",%n    \"age\": 99%n  }%n]"),
                MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}, {\"user\":\"foo\",\"age\": 99}]"));
        assertEquals(String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pet\": {%n    \"name\": \"fluffy\",%n    \"age\": 4%n  }%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pet\":{\"name\": \"fluffy\", \"age\": 4}}"));
        assertEquals(String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ]%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pets\":[\"fluffy\",\"hasso\"]}"));
        assertEquals(String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ],%n  \"age\": 32%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[\"fluffy\",\"hasso\"],\"age\": 32}"));
        assertEquals(String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    {%n      \"name\": \"fluffy\",%n      \"age\": 4%n    },%n    {%n      \"name\": \"hasso\",%n      \"age\": 2%n    }%n  ],%n  \"age\": 32%n}"),
                MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[{\"name\": \"fluffy\", \"age\": 4},{\"name\": \"hasso\", \"age\": 2}],\"age\": 32}"));
    }

    @Test
    public void shouldPrettyPrintXml() {
        assertEquals("", MessagePayloadUtils.prettyPrint(""));
        assertEquals(String.format("<root>%n</root>%n"),
                MessagePayloadUtils.prettyPrint("<root></root>"));
        assertEquals(String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint("<root><text>Citrus rocks!</text></root>"));
        assertEquals(String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<root>%n  <text>Citrus rocks!</text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><text>Citrus rocks!</text></root>"));
        assertEquals(String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint(String.format("<root>%n<text>%nCitrus rocks!%n</text>%n</root>")));
        assertEquals(String.format("<root>%n  <text language=\"eng\">Citrus rocks!</text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint(String.format("<root>%n  <text language=\"eng\">%nCitrus rocks!%n  </text>%n</root>")));
        assertEquals(String.format("<root>%n  <text language=\"eng\">%n    <![CDATA[Citrus rocks!]]>%n  </text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint(String.format("%n%n  <root><text language=\"eng\"><![CDATA[Citrus rocks!]]></text></root>")));
        assertEquals(String.format("<root>%n  <text language=\"eng\" important=\"true\">%n    <![CDATA[%n  Citrus rocks!%n  ]]>%n  </text>%n</root>%n"),
                MessagePayloadUtils.prettyPrint(String.format("<root><text language=\"eng\" important=\"true\"><![CDATA[%n  Citrus rocks!%n  ]]></text></root>")));
    }

}
