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

package org.citrusframework.xml.functions;

import org.citrusframework.base.functions.DefaultFunctionRegistry;
import org.citrusframework.functions.FunctionUtils;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.xml.functions.XmlFunctions.createCDataSection;
import static org.citrusframework.xml.functions.XmlFunctions.escapeXml;
import static org.citrusframework.xml.functions.XmlFunctions.xPath;

public class XmlFunctionsTest extends AbstractUnitTest {

    @Test
    public void testCreateCDataSection() {
        Assertions.assertEquals("<![CDATA[<Test><Message>Some Text</Message></Test>]]>", createCDataSection("<Test><Message>Some Text</Message></Test>", context));
    }

    @Test
    public void testEscapeXml() {
        Assertions.assertEquals("&lt;Test&gt;&lt;Message&gt;Some Text&lt;/Message&gt;&lt;/Test&gt;", escapeXml("<Test><Message>Some Text</Message></Test>", context));
    }

    @Test
    public void testXpath() {
        Assertions.assertEquals("Some Text", xPath("<Test><Message>Some Text</Message></Test>", "/Test/Message", context));
    }

    @Test
    public void testFunctionUtils() {
        context.setFunctionRegistry(new DefaultFunctionRegistry());
        Assertions.assertEquals("&lt;Message&gt;Hello Yes, I like Citrus!&lt;/Message&gt;", FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes, I like Citrus!</Message>')", context));
        Assertions.assertEquals("&lt;Message&gt;Hello Yes , I like Citrus!&lt;/Message&gt;", FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes , I like Citrus!</Message>')", context));
        Assertions.assertEquals("&lt;Message&gt;Hello Yes,I like Citrus, and this is great!&lt;/Message&gt;", FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes,I like Citrus, and this is great!</Message>')", context));

        Assertions.assertEquals("<![CDATA[<Message>Hello Citrus!</Message>]]>", FunctionUtils.resolveFunction("citrus:cdataSection('<Message>Hello Citrus!</Message>')", context));

        Assertions.assertEquals("Hello Citrus!", FunctionUtils.resolveFunction("citrus:xpath('<Message>Hello Citrus!</Message>', '/Message')", context));
    }
}
