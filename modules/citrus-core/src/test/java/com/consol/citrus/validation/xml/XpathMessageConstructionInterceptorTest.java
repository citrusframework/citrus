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

package com.consol.citrus.validation.xml;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
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
    public void testConstructWithXPath() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>");
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/TestMessage/Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .endsWith("<TestMessage><Text>Hello!</Text></TestMessage>"));
    }
    
    @Test
    public void testConstructWithXPathAndDefaultNamespace() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://www.citrusframework.org/test\">" +
                "<Text>Hello World!</Text>" +
                "</TestMessage>");
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/:TestMessage/:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<Text>Hello!</Text>"));
    }
    
    @Test
    public void testConstructWithXPathAndNamespace() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns0:Text>Hello World!</ns0:Text>" +
                "</ns0:TestMessage>");
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns0:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }
    
    @Test
    public void testConstructWithXPathAndNestedNamespace() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns1:Text xmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello World!</ns1:Text>" +
                "</ns0:TestMessage>");
        
        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/ns0:TestMessage/ns1:Text", "Hello!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns1:Textxmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello!</ns1:Text>"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Can not evaluate xpath expression.*")
    public void testConstructWithInvalidXPath() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>");

        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put(".Invalid/Unknown", "Hello!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "No result for XPath expression.*")
    public void testConstructWithXPathNoResult() {
        Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>");

        Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/TestMessage/Unknown", "Hello!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        interceptor.interceptMessage(message, CitrusConstants.DEFAULT_MESSAGE_TYPE, context);
    }
}
