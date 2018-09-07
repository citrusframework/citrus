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

import com.consol.citrus.Citrus;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Christoph Deppisch
 */
public class XpathMessageConstructionInterceptorTest extends AbstractTestNGUnitTest {

    private Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage><Text>Hello World!</Text></TestMessage>");

    private Message messageNamespace = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns0:Text>Hello World!</ns0:Text>" +
            "</ns0:TestMessage>");

    @Test
    public void testConstructWithXPath() {
        final Map<String, String> xPathExpressions = new HashMap<String, String>();
        xPathExpressions.put("/TestMessage/Text", "Hello!");
        
        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .endsWith("<TestMessage><Text>Hello!</Text></TestMessage>"));
    }
    
    @Test
    public void testConstructWithXPathAndDefaultNamespace() {
        final Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://www.citrusframework.org/test\">" +
                "<Text>Hello World!</Text>" +
                "</TestMessage>");
        
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/:TestMessage/:Text", "Hello!");
        
        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<Text>Hello!</Text>"));
    }
    
    @Test
    public void testConstructWithXPathAndNamespace() {
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/ns0:TestMessage/ns0:Text", "Hello!");
        
        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(messageNamespace, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }

    @Test
    public void testConstructWithXPathAndGlobalNamespace() {
        context.getNamespaceContextBuilder().getNamespaceMappings().put("global", "http://www.citrusframework.org/test");

        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/global:TestMessage/global:Text", "Hello!");

        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);

        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(messageNamespace, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }
    
    @Test
    public void testConstructWithXPathAndNestedNamespace() {
        final Message message = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns0:TestMessage xmlns:ns0=\"http://www.citrusframework.org/test\">" +
                "<ns1:Text xmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello World!</ns1:Text>" +
                "</ns0:TestMessage>");
        
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/ns0:TestMessage/ns1:Text", "Hello!");
        
        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        
        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns1:Textxmlns:ns1=\"http://www.citrusframework.org/test/text\">Hello!</ns1:Text>"));
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Can not evaluate xpath expression.*")
    public void testConstructWithInvalidXPath() {
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put(".Invalid/Unknown", "Hello!");

        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        interceptor.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "No result for XPath expression.*")
    public void testConstructWithXPathNoResult() {
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/TestMessage/Unknown", "Hello!");

        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);
        interceptor.interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Can not evaluate xpath expression.*")
    public void testConstructWithXPathAndInvalidGlobalNamespace() {
        final Map<String, String> xPathExpressions = new HashMap<>();
        xPathExpressions.put("/global:TestMessage/global:Text", "Hello!");

        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpressions);

        Assert.assertTrue(StringUtils.trimAllWhitespace(interceptor.interceptMessage(messageNamespace, Citrus.DEFAULT_MESSAGE_TYPE, context).getPayload(String.class))
                .contains("<ns0:Text>Hello!</ns0:Text>"));
    }

    @Test
    public void testAddTextToEmptyElement(){

        //GIVEN
        final Message message = new DefaultMessage(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<TestMessage>" +
                    "<Text></Text>" +
                "</TestMessage>");
        final Map<String, String> xPathExpression = Collections.singletonMap("//TestMessage/Text", "foobar");

        //WHEN
        final XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(xPathExpression);

        //THEN
        Assert.assertTrue(StringUtils
                .trimAllWhitespace(
                        interceptor
                                .interceptMessage(message, Citrus.DEFAULT_MESSAGE_TYPE, context)
                                .getPayload(String.class))
                .contains("<Text>foobar</Text>"));
    }
}
