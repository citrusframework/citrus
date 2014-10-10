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

package com.consol.citrus.util;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.*;

import javax.xml.XMLConstants;
import java.io.*;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class XMLUtilsTest {

    @Test
    public void testFindNodeByName() {
        Document doc = XMLUtils.parseMessagePayload(
                "<testRequest><message id=\"1\">Hello</message></testRequest>");

        Node result;

        result = XMLUtils.findNodeByName(doc, "testRequest");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getLocalName(), "testRequest");
        Assert.assertEquals(result.getNodeType(), Document.ELEMENT_NODE);

        result = XMLUtils.findNodeByName(doc, "testRequest.message");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getLocalName(), "message");
        Assert.assertEquals(result.getNodeType(), Document.ELEMENT_NODE);

        result = XMLUtils.findNodeByName(doc, "testRequest.message.id");
        Assert.assertNotNull(result);
        Assert.assertEquals(result.getLocalName(), "id");
        Assert.assertEquals(result.getNodeType(), Document.ATTRIBUTE_NODE);

        result = XMLUtils.findNodeByName(doc, "testRequest.wrongElement");
        Assert.assertNull(result);
    }

    @Test
    public void testStripWhitespaceNodes() {
        Node testNode = EasyMock.createMock(Node.class);
        Node textNode = EasyMock.createMock(Node.class);
        Node whiteSpaceNode = EasyMock.createMock(Node.class);

        reset(testNode, textNode, whiteSpaceNode);

        expect(testNode.getFirstChild()).andReturn(whiteSpaceNode).once();
        expect(testNode.getNodeType()).andReturn(Document.ELEMENT_NODE);
        expect(testNode.removeChild(whiteSpaceNode)).andReturn(testNode).once();

        expect(whiteSpaceNode.getNodeType()).andReturn(Document.TEXT_NODE);
        expect(whiteSpaceNode.getFirstChild()).andReturn(null).once();
        expect(whiteSpaceNode.getNextSibling()).andReturn(textNode).once();
        expect(whiteSpaceNode.getNodeValue()).andReturn("").once();
        expect(whiteSpaceNode.getParentNode()).andReturn(testNode).once();

        expect(textNode.getNodeType()).andReturn(Document.TEXT_NODE);
        expect(textNode.getFirstChild()).andReturn(null).once();
        expect(textNode.getNextSibling()).andReturn(null).once();
        expect(textNode.getNodeValue()).andReturn("This is a sample text").once();

        replay(testNode, textNode, whiteSpaceNode);

        XMLUtils.stripWhitespaceNodes(testNode);

        verify(testNode, textNode, whiteSpaceNode);
    }

    @Test
    public void testGetNodePathName() {
        Document doc = EasyMock.createMock(Document.class);
        Node testNode = EasyMock.createMock(Node.class);
        Node childNode1 = EasyMock.createMock(Node.class);
        Node childNode2 = EasyMock.createMock(Node.class);
        Node childNode3 = EasyMock.createMock(Node.class);

        reset(doc, testNode, childNode1, childNode2, childNode3);

        expect(doc.getParentNode()).andReturn(null).anyTimes();

        expect(testNode.getLocalName()).andReturn("testNode").anyTimes();
        expect(testNode.getParentNode()).andReturn(doc).anyTimes();

        expect(childNode1.getLocalName()).andReturn("childNode1").anyTimes();
        expect(childNode1.getParentNode()).andReturn(testNode).anyTimes();

        expect(childNode2.getLocalName()).andReturn("childNode2").anyTimes();
        expect(childNode2.getParentNode()).andReturn(testNode).anyTimes();

        expect(childNode3.getLocalName()).andReturn("childNode3").anyTimes();
        expect(childNode3.getParentNode()).andReturn(childNode2).anyTimes();

        expect(testNode.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();
        expect(childNode1.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();
        expect(childNode2.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();
        expect(childNode3.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();

        replay(doc, testNode, childNode1, childNode2, childNode3);

        Assert.assertEquals(XMLUtils.getNodesPathName(testNode), "testNode");
        Assert.assertEquals(XMLUtils.getNodesPathName(childNode1), "testNode.childNode1");
        Assert.assertEquals(XMLUtils.getNodesPathName(childNode3), "testNode.childNode2.childNode3");

        verify(doc, testNode, childNode1, childNode2, childNode3);
    }

    @Test
    public void testGetNodePathNameForAttribute() {
        Document doc = EasyMock.createMock(Document.class);
        Element testNode = EasyMock.createMock(Element.class);
        Element childNode1 = EasyMock.createMock(Element.class);
        Attr attribute1 = EasyMock.createMock(Attr.class);
        Attr attribute2 = EasyMock.createMock(Attr.class);

        reset(doc, testNode, childNode1, attribute1, attribute2);

        expect(doc.getParentNode()).andReturn(null).anyTimes();

        expect(testNode.getLocalName()).andReturn("testNode").anyTimes();
        expect(testNode.getParentNode()).andReturn(doc).anyTimes();

        expect(childNode1.getLocalName()).andReturn("childNode1").anyTimes();
        expect(childNode1.getParentNode()).andReturn(testNode).anyTimes();

        expect(attribute1.getLocalName()).andReturn("attribute1").anyTimes();
        expect(attribute1.getOwnerElement()).andReturn(testNode).anyTimes();

        expect(attribute2.getLocalName()).andReturn("attribute2").anyTimes();
        expect(attribute2.getOwnerElement()).andReturn(childNode1).anyTimes();

        expect(testNode.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();
        expect(childNode1.getNodeType()).andReturn(Node.ELEMENT_NODE).anyTimes();
        expect(attribute1.getNodeType()).andReturn(Node.ATTRIBUTE_NODE).anyTimes();
        expect(attribute2.getNodeType()).andReturn(Node.ATTRIBUTE_NODE).anyTimes();

        replay(doc, testNode, childNode1, attribute1, attribute2);

        Assert.assertEquals(XMLUtils.getNodesPathName(testNode), "testNode");
        Assert.assertEquals(XMLUtils.getNodesPathName(attribute1), "testNode.attribute1");
        Assert.assertEquals(XMLUtils.getNodesPathName(attribute2), "testNode.childNode1.attribute2");

        verify(doc, testNode, childNode1, attribute1, attribute2);
    }

    @Test
    public void testPrettyPrint() throws IOException {
        String xml = "<testRequest><message>Hello</message></testRequest>";

        int lines = 0;
        BufferedReader reader = null;
        
        String prettyprint = XMLUtils.prettyPrint(xml);
        try {
            reader = new BufferedReader(new StringReader(prettyprint));
            while (reader.readLine() != null) {
                lines++;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        
        Assert.assertTrue(prettyprint.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        Assert.assertTrue(lines > 0);
    }

    @Test
    public void testLookupNamespacesInXMLFragment() {
        Map<String, String> namespaces;

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/xmlns/test\" xmlns:ns2=\"http://www.consol.de/xmlns/test2\"></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/xmlns/test\" xmlns:ns2=\"http://www.consol.de/xmlns/test2\"></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");
    }

    @Test
    public void testLookupNamespacesInXMLFragmentSingleQuotes() {
        Map<String, String> namespaces;

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://www.consol.de/test' xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://www.consol.de/xmlns/test' xmlns:ns2='http://www.consol.de/xmlns/test2'></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1='http://www.consol.de/test' xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" xmlns:ns2='http://www.consol.de/test2'></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1='http://www.consol.de/xmlns/test' xmlns:ns2='http://www.consol.de/xmlns/test2'></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/xmlns/test2");
    }

    @Test
    public void testLookupNamespacesInXMLFragmentWithAtributes() {
        Map<String, String> namespaces;

        namespaces = XMLUtils.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" id=\"123456789\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://www.consol.de/test\" id=\"123456789\" xmlns:ns2=\"http://www.consol.de/test2\"></ns1:testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");
        Assert.assertEquals(namespaces.get("ns2"), "http://www.consol.de/test2");
    }

    @Test
    public void testLookupNamespacesInXMLFragmentDefaultNamespaces() {
        Map<String, String> namespaces;

        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/test-default\"></testRequest>");

        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");

        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns='http://www.consol.de/test-default'></testRequest>");

        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");

        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/test-default\" xmlns:ns1=\"http://www.consol.de/test\"></testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");

        namespaces = XMLUtils.lookupNamespaces("<testRequest xmlns=\"http://www.consol.de/xmlns/test-default\" xmlns:ns1=\"http://www.consol.de/xmlns/test\"></testRequest>");

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/xmlns/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://www.consol.de/test-default\"></testRequest>"));

        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns='http://www.consol.de/test-default'></testRequest>"));

        Assert.assertEquals(namespaces.size(), 1);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://www.consol.de/test-default\" xmlns:ns1=\"http://www.consol.de/test\"></testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/test");

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://www.consol.de/xmlns/test-default\" xmlns:ns1=\"http://www.consol.de/xmlns/test\"></testRequest>"));

        Assert.assertEquals(namespaces.size(), 2);
        Assert.assertEquals(namespaces.get(XMLConstants.DEFAULT_NS_PREFIX), "http://www.consol.de/xmlns/test-default");
        Assert.assertEquals(namespaces.get("ns1"), "http://www.consol.de/xmlns/test");
    }

    @Test
    public void testLookupNamespacesInXMLFragmentNoNamespacesFound() {
        Map<String, String> namespaces = XMLUtils.lookupNamespaces("<testRequest id=\"123456789\"></testRequest>");

        Assert.assertEquals(namespaces.size(), 0);
    }

    @Test
    public void testParseEncodingCharset() {
        Document doc = XMLUtils.parseMessagePayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        		"<testRequest xmlns=\"http://www.consol.de/test-default\"></testRequest>");
        Assert.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0' encoding='UTF-8'?>" +
        		"<testRequest xmlns='http://www.consol.de/test-default'></testRequest>");
        Assert.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0' encoding = 'ISO-8859-1' standalone=\"yes\"?>" +
        		"<testRequest xmlns='http://www.consol.de/test-default'></testRequest>");
        Assert.assertNotNull(doc);
    }

    @Test
    public void testEncodingRoundTrip() throws Exception {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<testRequest xmlns=\"http://www.consol.de/test-default\">ÄäÖöÜü</testRequest>";

        Document doc = XMLUtils.parseMessagePayload(payload);

        Assert.assertEquals(XMLUtils.serialize(doc), payload + System.getProperty("line.separator"));
    }

    @Test
    public void testOmitXmlDeclaration() throws Exception {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <testRequest xmlns=\"http://www.consol.de/test-default\">Test</testRequest>";

        Assert.assertEquals(XMLUtils.omitXmlDeclaration(payload), "<testRequest xmlns=\"http://www.consol.de/test-default\">Test</testRequest>");
        Assert.assertEquals(XMLUtils.omitXmlDeclaration("<testRequest xmlns=\"http://www.consol.de/test-default\">Test</testRequest>"), "<testRequest xmlns=\"http://www.consol.de/test-default\">Test</testRequest>");
        Assert.assertEquals(XMLUtils.omitXmlDeclaration(""), "");
        Assert.assertEquals(XMLUtils.omitXmlDeclaration("Test"), "Test");
    }
}
