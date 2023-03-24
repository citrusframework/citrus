/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.functions;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.functions.XmlFunctions.createCDataSection;
import static org.citrusframework.functions.XmlFunctions.escapeXml;
import static org.citrusframework.functions.XmlFunctions.xPath;

public class XmlFunctionsTest extends AbstractTestNGUnitTest {

    @Test
    public void testCreateCDataSection() throws Exception {
        Assert.assertEquals(createCDataSection("<Test><Message>Some Text</Message></Test>", context), "<![CDATA[<Test><Message>Some Text</Message></Test>]]>");
    }

    @Test
    public void testEscapeXml() throws Exception {
        Assert.assertEquals(escapeXml("<Test><Message>Some Text</Message></Test>", context), "&lt;Test&gt;&lt;Message&gt;Some Text&lt;/Message&gt;&lt;/Test&gt;");
    }

    @Test
    public void testXpath() throws Exception {
        Assert.assertEquals(xPath("<Test><Message>Some Text</Message></Test>", "/Test/Message", context), "Some Text");
    }

    @Test
    public void testFunctionUtils() {
        context.setFunctionRegistry(new DefaultFunctionRegistry());
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes, I like Citrus!</Message>')", context), "&lt;Message&gt;Hello Yes, I like Citrus!&lt;/Message&gt;");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes , I like Citrus!</Message>')", context), "&lt;Message&gt;Hello Yes , I like Citrus!&lt;/Message&gt;");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes,I like Citrus, and this is great!</Message>')", context), "&lt;Message&gt;Hello Yes,I like Citrus, and this is great!&lt;/Message&gt;");

        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:cdataSection('<Message>Hello Citrus!</Message>')", context), "<![CDATA[<Message>Hello Citrus!</Message>]]>");

        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:xpath('<Message>Hello Citrus!</Message>', '/Message')", context), "Hello Citrus!");
    }
}
