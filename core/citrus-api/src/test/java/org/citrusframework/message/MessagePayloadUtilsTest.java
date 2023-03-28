/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.message;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MessagePayloadUtilsTest {

    @Test
    public void shouldPrettyPrintJson() {
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(""), "");
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{}"), "{}");
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("[]"), "[]");
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\"}"),
                String.format("{%n  \"user\": \"citrus\"%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"text\":\"<?;,{}' '[]:>\"}"),
                String.format("{%n  \"text\": \"<?;,{}' '[]:>\"%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(String.format("%n%n  {  \"user\":%n%n \"citrus\"  }")),
                String.format("{%n  \"user\": \"citrus\"%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("[22, 32]"),
                String.format("[%n22,%n32%n]"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}]"),
                String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  }%n]"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("[{\"user\":\"citrus\",\"age\": 32}, {\"user\":\"foo\",\"age\": 99}]"),
                String.format("[%n  {%n    \"user\": \"citrus\",%n    \"age\": 32%n  },%n  {%n    \"user\": \"foo\",%n    \"age\": 99%n  }%n]"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pet\":{\"name\": \"fluffy\", \"age\": 4}}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pet\": {%n    \"name\": \"fluffy\",%n    \"age\": 4%n  }%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"age\": 32,\"pets\":[\"fluffy\",\"hasso\"]}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"age\": 32,%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ]%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[\"fluffy\",\"hasso\"],\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    \"fluffy\",%n    \"hasso\"%n  ],%n  \"age\": 32%n}"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("{\"user\":\"citrus\",\"pets\":[{\"name\": \"fluffy\", \"age\": 4},{\"name\": \"hasso\", \"age\": 2}],\"age\": 32}"),
                String.format("{%n  \"user\": \"citrus\",%n  \"pets\": [%n    {%n      \"name\": \"fluffy\",%n      \"age\": 4%n    },%n    {%n      \"name\": \"hasso\",%n      \"age\": 2%n    }%n  ],%n  \"age\": 32%n}"));
    }

    @Test
    public void shouldPrettyPrintXml() {
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(""), "");
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("<root></root>"),
                String.format("<root>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("<root><text>Citrus rocks!</text></root>"),
                String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><text>Citrus rocks!</text></root>"),
                String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>%n<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root>%n<text>%nCitrus rocks!%n</text>%n</root>")),
                String.format("<root>%n  <text>Citrus rocks!</text>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root>%n  <text language=\"eng\">%nCitrus rocks!%n  </text>%n</root>")),
                String.format("<root>%n  <text language=\"eng\">Citrus rocks!</text>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(String.format("%n%n  <root><text language=\"eng\"><![CDATA[Citrus rocks!]]></text></root>")),
                String.format("<root>%n  <text language=\"eng\">%n    <![CDATA[Citrus rocks!]]>%n  </text>%n</root>%n"));
        Assert.assertEquals(MessagePayloadUtils.prettyPrint(String.format("<root><text language=\"eng\" important=\"true\"><![CDATA[%n  Citrus rocks!%n  ]]></text></root>")),
                String.format("<root>%n  <text language=\"eng\" important=\"true\">%n    <![CDATA[%n  Citrus rocks!%n  ]]>%n  </text>%n</root>%n"));
    }

}