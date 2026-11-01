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

package org.citrusframework.xml.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import javax.xml.XMLConstants;

import org.citrusframework.api.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.support.XMLUtils;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class XMLUtilsTest {

    @Test
    public void testFindNodeByName() {
        Document doc = XMLUtils.parseMessagePayload(
                "<testRequest><message id=\"1\">Hello</message></testRequest>");

        Node result;

        result = XMLUtils.findNodeByName(doc, "testRequest");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("testRequest", result.getLocalName());
        Assertions.assertEquals(Document.ELEMENT_NODE, result.getNodeType());

        result = XMLUtils.findNodeByName(doc, "testRequest.message");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("message", result.getLocalName());
        Assertions.assertEquals(Document.ELEMENT_NODE, result.getNodeType());

        result = XMLUtils.findNodeByName(doc, "testRequest.message.id");
        Assertions.assertNotNull(result);
        Assertions.assertEquals("id", result.getLocalName());
        Assertions.assertEquals(Document.ATTRIBUTE_NODE, result.getNodeType());

        result = XMLUtils.findNodeByName(doc, "testRequest.wrongElement");
        Assertions.assertNull(result);
    }

    @Test
    public void testStripWhitespaceNodes() {
        Node testNode = Mockito.mock(Node.class);
        Node textNode = Mockito.mock(Node.class);
        Node whiteSpaceNode = Mockito.mock(Node.class);

        reset(testNode, textNode, whiteSpaceNode);

        when(testNode.getFirstChild()).thenReturn(whiteSpaceNode);
        when(testNode.getNodeType()).thenReturn(Document.ELEMENT_NODE);
        when(testNode.removeChild(whiteSpaceNode)).thenReturn(testNode);

        when(whiteSpaceNode.getNodeType()).thenReturn(Document.TEXT_NODE);
        when(whiteSpaceNode.getFirstChild()).thenReturn(null);
        when(whiteSpaceNode.getNextSibling()).thenReturn(textNode);
        when(whiteSpaceNode.getNodeValue()).thenReturn("");
        when(whiteSpaceNode.getParentNode()).thenReturn(testNode);

        when(textNode.getNodeType()).thenReturn(Document.TEXT_NODE);
        when(textNode.getFirstChild()).thenReturn(null);
        when(textNode.getNextSibling()).thenReturn(null);
        when(textNode.getNodeValue()).thenReturn("This is a sample text");

        XMLUtils.stripWhitespaceNodes(testNode);

    }

    @Test
    public void testGetNodePathName() {
        Document doc = Mockito.mock(Document.class);
        Node testNode = Mockito.mock(Node.class);
        Node childNode1 = Mockito.mock(Node.class);
        Node childNode2 = Mockito.mock(Node.class);
        Node childNode3 = Mockito.mock(Node.class);

        reset(doc, testNode, childNode1, childNode2, childNode3);

        when(doc.getParentNode()).thenReturn(null);

        when(testNode.getLocalName()).thenReturn("testNode");
        when(testNode.getParentNode()).thenReturn(doc);

        when(childNode1.getLocalName()).thenReturn("childNode1");
        when(childNode1.getParentNode()).thenReturn(testNode);

        when(childNode2.getLocalName()).thenReturn("childNode2");
        when(childNode2.getParentNode()).thenReturn(testNode);

        when(childNode3.getLocalName()).thenReturn("childNode3");
        when(childNode3.getParentNode()).thenReturn(childNode2);

        when(testNode.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(childNode1.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(childNode2.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(childNode3.getNodeType()).thenReturn(Node.ELEMENT_NODE);

        Assertions.assertEquals("testNode", XMLUtils.getNodesPathName(testNode));
        Assertions.assertEquals("testNode.childNode1", XMLUtils.getNodesPathName(childNode1));
        Assertions.assertEquals("testNode.childNode2.childNode3", XMLUtils.getNodesPathName(childNode3));

    }

    @Test
    public void testGetNodePathNameForAttribute() {
        Document doc = Mockito.mock(Document.class);
        Element testNode = Mockito.mock(Element.class);
        Element childNode1 = Mockito.mock(Element.class);
        Attr attribute1 = Mockito.mock(Attr.class);
        Attr attribute2 = Mockito.mock(Attr.class);

        reset(doc, testNode, childNode1, attribute1, attribute2);

        when(doc.getParentNode()).thenReturn(null);

        when(testNode.getLocalName()).thenReturn("testNode");
        when(testNode.getParentNode()).thenReturn(doc);

        when(childNode1.getLocalName()).thenReturn("childNode1");
        when(childNode1.getParentNode()).thenReturn(testNode);

        when(attribute1.getLocalName()).thenReturn("attribute1");
        when(attribute1.getOwnerElement()).thenReturn(testNode);

        when(attribute2.getLocalName()).thenReturn("attribute2");
        when(attribute2.getOwnerElement()).thenReturn(childNode1);

        when(testNode.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(childNode1.getNodeType()).thenReturn(Node.ELEMENT_NODE);
        when(attribute1.getNodeType()).thenReturn(Node.ATTRIBUTE_NODE);
        when(attribute2.getNodeType()).thenReturn(Node.ATTRIBUTE_NODE);

        Assertions.assertEquals("testNode", XMLUtils.getNodesPathName(testNode));
        Assertions.assertEquals("testNode.attribute1", XMLUtils.getNodesPathName(attribute1));
        Assertions.assertEquals("testNode.childNode1.attribute2", XMLUtils.getNodesPathName(attribute2));

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

        Assertions.assertTrue(prettyprint.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        Assertions.assertTrue(lines > 0);
    }

    @Test
    public void testLookupNamespacesInXMLFragment() {
        Map<String, String> namespaces;

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" xmlns:ns2=\"http://citrusframework.org/test2\"></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/xmlns/test\" xmlns:ns2=\"http://citrusframework.org/xmlns/test2\"></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" xmlns:ns2=\"http://citrusframework.org/test2\"></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/xmlns/test\" xmlns:ns2=\"http://citrusframework.org/xmlns/test2\"></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test2", namespaces.get("ns2"));
    }

    @Test
    public void testLookupNamespacesInXMLFragmentSingleQuotes() {
        Map<String, String> namespaces;

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://citrusframework.org/test' xmlns:ns2='http://citrusframework.org/test2'></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" xmlns:ns2='http://citrusframework.org/test2'></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1='http://citrusframework.org/xmlns/test' xmlns:ns2='http://citrusframework.org/xmlns/test2'></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1='http://citrusframework.org/test' xmlns:ns2='http://citrusframework.org/test2'></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" xmlns:ns2='http://citrusframework.org/test2'></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1='http://citrusframework.org/xmlns/test' xmlns:ns2='http://citrusframework.org/xmlns/test2'></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test2", namespaces.get("ns2"));
    }

    @Test
    public void testLookupNamespacesInXMLFragmentWithAtributes() {
        Map<String, String> namespaces;

        namespaces = NamespaceContextBuilder.lookupNamespaces("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" id=\"123456789\" xmlns:ns2=\"http://citrusframework.org/test2\"></ns1:testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<ns1:testRequest xmlns:ns1=\"http://citrusframework.org/test\" id=\"123456789\" xmlns:ns2=\"http://citrusframework.org/test2\"></ns1:testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));
        Assertions.assertEquals("http://citrusframework.org/test2", namespaces.get("ns2"));
    }

    @Test
    public void testLookupNamespacesInXMLFragmentDefaultNamespaces() {
        Map<String, String> namespaces;

        namespaces = NamespaceContextBuilder.lookupNamespaces("<testRequest xmlns=\"http://citrusframework.org/test-default\"></testRequest>");

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<testRequest xmlns='http://citrusframework.org/test-default'></testRequest>");

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<testRequest xmlns=\"http://citrusframework.org/test-default\" xmlns:ns1=\"http://citrusframework.org/test\"></testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));

        namespaces = NamespaceContextBuilder.lookupNamespaces("<testRequest xmlns=\"http://citrusframework.org/xmlns/test-default\" xmlns:ns1=\"http://citrusframework.org/xmlns/test\"></testRequest>");

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://citrusframework.org/test-default\"></testRequest>"));

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns='http://citrusframework.org/test-default'></testRequest>"));

        Assertions.assertEquals(1, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://citrusframework.org/test-default\" xmlns:ns1=\"http://citrusframework.org/test\"></testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));
        Assertions.assertEquals("http://citrusframework.org/test", namespaces.get("ns1"));

        namespaces = XMLUtils.lookupNamespaces(
                XMLUtils.parseMessagePayload("<testRequest xmlns=\"http://citrusframework.org/xmlns/test-default\" xmlns:ns1=\"http://citrusframework.org/xmlns/test\"></testRequest>"));

        Assertions.assertEquals(2, namespaces.size());
        Assertions.assertEquals("http://citrusframework.org/xmlns/test-default", namespaces.get(XMLConstants.DEFAULT_NS_PREFIX));
        Assertions.assertEquals("http://citrusframework.org/xmlns/test", namespaces.get("ns1"));
    }

    @Test
    public void testLookupNamespacesInXMLFragmentNoNamespacesFound() {
        Map<String, String> namespaces = NamespaceContextBuilder.lookupNamespaces("<testRequest id=\"123456789\"></testRequest>");

        Assertions.assertEquals(0, namespaces.size());
    }

    @Test
    public void testParseEncodingCharset() {
        Document doc = XMLUtils.parseMessagePayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        		"<testRequest xmlns=\"http://citrusframework.org/test-default\"></testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0' encoding='UTF-8'?>" +
        		"<testRequest xmlns='http://citrusframework.org/test-default'></testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0' encoding = 'ISO-8859-1' standalone=\"yes\"?>" +
        		"<testRequest xmlns='http://citrusframework.org/test-default'></testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0'?>" +
        		"<testRequest xmlns='http://citrusframework.org/test-default'></testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0'?>" +
                "<testRequest xmlns='http://citrusframework.org/test-default'>encoding</testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0' encoding='UTF-8'?>" +
                "<testRequest xmlns='http://citrusframework.org/test-default'><![CDATA[<?xml version='1.0' encoding='some unknown encoding'?><message>Nested</message>]]></testRequest>");
        Assertions.assertNotNull(doc);

        doc = XMLUtils.parseMessagePayload("<?xml version='1.0'?>" +
                "<testRequest xmlns='http://citrusframework.org/test-default'><![CDATA[<?xml version='1.0' encoding='some unknown encoding'?><message>Nested</message>]]></testRequest>");
        Assertions.assertNotNull(doc);
    }

    @Test
    public void testEncodingRoundTrip() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<testRequest xmlns=\"http://citrusframework.org/test-default\">ÄäÖöÜü</testRequest>";

        Document doc = XMLUtils.parseMessagePayload(payload);

        Assertions.assertEquals("""
                <?xml version="1.0" encoding="UTF-8"?>
                <testRequest xmlns="http://citrusframework.org/test-default">ÄäÖöÜü</testRequest>
                """,
                        XMLUtils.serialize(doc));
    }

    @Test
    public void testOmitXmlDeclaration() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <testRequest xmlns=\"http://citrusframework.org/test-default\">Test</testRequest>";

        Assertions.assertEquals("<testRequest xmlns=\"http://citrusframework.org/test-default\">Test</testRequest>", XMLUtils.omitXmlDeclaration(payload));
        Assertions.assertEquals("<testRequest xmlns=\"http://citrusframework.org/test-default\">Test</testRequest>", XMLUtils.omitXmlDeclaration("<testRequest xmlns=\"http://citrusframework.org/test-default\">Test</testRequest>"));
        Assertions.assertEquals("", XMLUtils.omitXmlDeclaration(""));
        Assertions.assertEquals("Test", XMLUtils.omitXmlDeclaration("Test"));
    }
}
