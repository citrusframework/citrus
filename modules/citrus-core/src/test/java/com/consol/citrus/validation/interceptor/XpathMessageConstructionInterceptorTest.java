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

package com.consol.citrus.validation.interceptor;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class XpathMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {
    
    @Test
    public void testReplaceMessageValuesWithXPath() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/TestMessage/Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessagePayload(messagePayload, CitrusConstants.DEFAULT_MESSAGE_TYPE, context))
                .endsWith("<TestMessage><Text>Hello!</Text></TestMessage>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndDefaultNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://www.citrusframework.org/test\">" +
                "<Text>Hello World!</Text>" +
                "</TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/:TestMessage/:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessagePayload(messagePayload, CitrusConstants.DEFAULT_MESSAGE_TYPE, context))
                .contains("<Text>Hello!</Text>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns0:Text>Hello World!</ns0:Text>" +
                "</ns0:TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns0:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessagePayload(messagePayload, CitrusConstants.DEFAULT_MESSAGE_TYPE, context))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }
    
    @Test
    public void testReplaceMessageValuesWithXPathAndNestedNamespace() {
        String messagePayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns1:Text xmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello World!</ns1:Text>" +
                "</ns0:TestMessage>";
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns1:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessagePayload(messagePayload, CitrusConstants.DEFAULT_MESSAGE_TYPE, context))
                .contains("<ns1:Textxmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello!</ns1:Text>"));
    }
}
