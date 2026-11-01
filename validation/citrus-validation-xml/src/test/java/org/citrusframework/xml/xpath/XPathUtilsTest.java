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

package org.citrusframework.xml.xpath;

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFactory;

import org.citrusframework.xml.support.XMLUtils;
import org.citrusframework.api.xml.namespace.DefaultNamespaceContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

public class XPathUtilsTest {

    @Test
    public void testDynamicNamespaceCreation() {
        Map<String, String> namespaces;
        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/foo}bar");

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/foo", namespaces.get("dns1"));

        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar/{http://citrusframework.org/foo}value");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/foo", namespaces.get("dns1"));
        Assertions.assertEquals("http://citrusframework.org/bar", namespaces.get("dns2"));

        namespaces = XPathUtils.getDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar/{urn:citrus}value");

        Assertions.assertEquals(3, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/foo", namespaces.get("dns1"));
        Assertions.assertEquals("http://citrusframework.org/bar", namespaces.get("dns2"));
        Assertions.assertEquals("urn:citrus", namespaces.get("dns3"));
    }

    @Test
    public void testDynamicNamespaceReplacement() {
        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("ns1", "http://citrusframework.org/foo");
        namespaces.put("ns2", "http://citrusframework.org/bar");
        namespaces.put("ns3", "http://citrusframework.org/foo-bar");

        Assertions.assertEquals("//ns1:Foo/ns1:bar",
                XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/foo}bar", namespaces));

        Assertions.assertEquals("//ns1:Foo/ns2:bar",
                XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo}Foo/{http://citrusframework.org/bar}bar", namespaces));

        Assertions.assertEquals("//ns3:Foo/bar",
                XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/foo-bar}Foo/bar", namespaces));

        Assertions.assertEquals("//{http://citrusframework.org/unkown}Foo/{http://citrusframework.org/unknown}bar",
                XPathUtils.replaceDynamicNamespaces("//{http://citrusframework.org/unkown}Foo/{http://citrusframework.org/unknown}bar", namespaces));
    }

    @Test
    public void testEvaluate() {
        Document personNode = XMLUtils.parseMessagePayload("<person status=\"single\">" +
                    "<name>foo</name>" +
                    "<age>23</age>" +
                "</person>");

        NamespaceContext namespaceContext = new DefaultNamespaceContext();

        Assertions.assertEquals("foo", XPathUtils.evaluate(personNode, "/person/name", namespaceContext, XPathExpressionResult.STRING));
        Assertions.assertEquals(23.0D, XPathUtils.evaluate(personNode, "/person/age", namespaceContext, XPathExpressionResult.NUMBER));
        Assertions.assertEquals(23, XPathUtils.evaluate(personNode, "/person/age", namespaceContext, XPathExpressionResult.INTEGER));
        Assertions.assertEquals("foo", XPathUtils.evaluateAsNode(personNode, "/person/name", namespaceContext).getFirstChild().getNodeValue());
        Assertions.assertEquals("foo", XPathUtils.evaluateAsNodeList(personNode, "/person/name", namespaceContext).item(0).getFirstChild().getNodeValue());
        Assertions.assertEquals(0L, XPathUtils.evaluateAsNodeList(personNode, "/person/unknown", namespaceContext).getLength());
        Assertions.assertTrue(XPathUtils.evaluateAsBoolean(personNode, "/person/name", namespaceContext));
        Assertions.assertFalse(XPathUtils.evaluateAsBoolean(personNode, "/person/unknown", namespaceContext));
        Assertions.assertEquals("foo", XPathUtils.evaluateAsString(personNode, "/person/name", namespaceContext));
        Assertions.assertEquals("foo", XPathUtils.evaluateAsObject(personNode, "/person/name", namespaceContext, new QName("http://www.w3.org/1999/XSL/Transform", "STRING")));
        Assertions.assertEquals(Double.valueOf(23.0D), XPathUtils.evaluateAsNumber(personNode, "/person/age", namespaceContext));
        Assertions.assertEquals("single", XPathUtils.evaluateAsString(personNode, "/person/@status", namespaceContext));
    }

    @Test
    public void testCustomXPathFactory() {
        System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, "");
        testEvaluate();
    }
}
