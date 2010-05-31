/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.util;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.Map;

import javax.xml.XMLConstants;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
        
        replay(doc, testNode, childNode1, childNode2, childNode3);
        
        Assert.assertEquals(XMLUtils.getNodesPathName(testNode), "testNode");
        Assert.assertEquals(XMLUtils.getNodesPathName(childNode1), "testNode.childNode1");
        Assert.assertEquals(XMLUtils.getNodesPathName(childNode3), "testNode.childNode2.childNode3");
        
        verify(doc, testNode, childNode1, childNode2, childNode3);
    }
    
    @Test
    public void testPrettyPrint() {
        String xml = "<testRequest><message>Hello</message></testRequest>";
        
        Assert.assertEquals(XMLUtils.prettyPrint(xml).trim(), 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<testRequest>\n    <message>Hello</message>\n</testRequest>");
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
}
