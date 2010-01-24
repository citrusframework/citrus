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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.LSResolverImpl;
import com.consol.citrus.xml.NamespaceContextImpl;

/**
 * Class providing several utility methods for XML processing.
 *
 * @author Jan Szczepanski, Christoph Deppisch
 * @since 2006
 *
 */
public class XMLUtils {
    /** DOM implementation */
    private static DOMImplementationRegistry registry = null;
    private static DOMImplementationLS domImpl = null;

    /**
     * Searches for a node within a DOM document with a given node path expression.
     * Elements are separated by '.' characters.
     * Example: Foo.Bar.Poo
     * @param doc DOM Document to search for a node.
     * @param pathExpression dot separated path expression
     * @return Node element found in the DOM document.
     */
    public static Node findNodeByName(Document doc, String pathExpression) {
        final StringTokenizer tok = new StringTokenizer(pathExpression, ".");
        final int numToks = tok.countTokens();
        NodeList elements;
        if (numToks == 1) {
            elements = doc.getElementsByTagNameNS("*", pathExpression);
            return elements.item(0);
        }

        String element = pathExpression.substring(pathExpression.lastIndexOf('.')+1);
        elements = doc.getElementsByTagNameNS("*", element);

        String attributeName = null;
        if (elements.getLength() == 0) {
            //No element found, but maybe we are searching for an attribute
            attributeName = element;

            //cut off attributeName and set element to next token and continue
            pathExpression = pathExpression.substring(0, pathExpression.length()-attributeName.length()-1);
            Node found = findNodeByName(doc, pathExpression);

            if (found != null) {
                return found.getAttributes().getNamedItem(attributeName);
            } else {
                 return null;
            }
        }

        StringBuffer pathName;
        Node parent;
        for(int j=0; j<elements.getLength(); j++) {
            int cnt = numToks-1;
            pathName = new StringBuffer(element);
            parent = elements.item(j).getParentNode();
            do {
                if (parent != null) {
                    pathName.insert(0, '.');
                    pathName.insert(0, parent.getLocalName());//getNodeName());
                    
                    parent = parent.getParentNode();
                }
            } while (parent != null && --cnt > 0);
            if (pathName.toString().equals(pathExpression)) {return elements.item(j);}
        }

        return null;
    }
    
    /**
     * Find node element by XPath expression.
     * @param node in DOM document.
     * @param expressionStr XPath expression string.
     * @return
     */
    public static Node findNodeByXPath(Node node, String expressionStr) {
        return findNodeByXPath(node, expressionStr, null);
    }

    /**
     * Finds a node in the DOM tree using XPath expressions.
     * @param node the XML node
     * @param xpath the XPath expression
     * @throws CitrusRuntimeException
     * @return the node element found in the DOM tree.
     */
    public static Node findNodeByXPath(Node node, String expressionStr, NamespaceContext nsContext) {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            
            if(nsContext == null) {
                nsContext = buildNamespaceContext(node);
            }
            
            xpath.setNamespaceContext(nsContext);
            
            XPathExpression expression = xpath.compile(expressionStr);
            Node found = (Node)expression.evaluate(node, XPathConstants.NODE);
                
            if (found == null) {
                throw new CitrusRuntimeException("Could not find node in XML tree for expression: " + expressionStr);
            }

            return found;
        } catch (XPathExpressionException e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Build a namespace context from a node element.
     * @param node holding namespace declarations.
     * @return the namespace context.
     */
    private static NamespaceContext buildNamespaceContext(Node node) {
        NamespaceContextImpl nsContext = new NamespaceContextImpl();
        
        if(node.getNodeType() == Node.DOCUMENT_NODE) {
            nsContext.setNamespaces(lookupNamespaces(node.getFirstChild()));
        } else {
            nsContext.setNamespaces(lookupNamespaces(node));
        }
        
        return nsContext;
    }

    /**
     * Evaluates a XPath expression towards a node element.
     * @param node element in a DOM tree.
     * @param expressionStr XPath expression string.
     * @return value of the found element.
     */
    public static String evaluateXPathExpression(Node node, String expressionStr) {
        return evaluateXPathExpression(node, expressionStr, null);
    }

    /**
     * Evaluates the XPath expression to return the respective value.
     * @param node element in a DOM tree.
     * @throws CitrusRuntimeException
     * @return the evaluated node value.
     */
    public static String evaluateXPathExpression(Node node, String expressionStr, NamespaceContext nsContext)  {
        try {
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            
            if(nsContext != null) {
                xpath.setNamespaceContext(nsContext);
            } else {
                xpath.setNamespaceContext(buildNamespaceContext(node));
            }
            
            XPathExpression expression = xpath.compile(expressionStr);
            String value = expression.evaluate(node);

            //in case value is empty check that DOM node really exists
            //if DOM node can not be found the xpath expression might be invalid
            if (value == null || value.length() == 0) {
                findNodeByXPath(node, expressionStr, nsContext);
            }
            
            return value;
        } catch (XPathExpressionException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Method to find out whether an expression is of XPath nature or custom dot notation syntax.
     * @param expression the expression string to check.
     * @return boolean the result.
     */
    public static boolean isXPathExpression(String expression) {
        return expression.indexOf("/") != (-1) || expression.indexOf("(") != (-1);
    }

    /**
     * Removes text nodes that are only containing whitespace characters
     * inside a DOM tree.
     *
     * @param element the root node to normalize.
     */
    public static void stripWhitespaceNodes(Node element) {
        Node node, child;
        for (child = element.getFirstChild(); child != null; child = node) {
            node = child.getNextSibling();
            stripWhitespaceNodes(child);
        }

        if (element.getNodeType() == Node.TEXT_NODE && element.getNodeValue().trim().length()==0) {
            element.getParentNode().removeChild(element);
        }
    }

    /**
     * Returns the path expression for a given node.
     * Path expressions look like: Foo.Bar.Poo where elements are
     * separated with a dot character.
     * 
     * @param node in DOM tree.
     * @return the path expression representing the node in DOM tree.
     */
    public static String getNodesPathName(Node node) {
        final StringBuffer buffer = new StringBuffer();
        buildNodeName(node, buffer);
        return buffer.toString();
    }

    /**
     * Builds the node path expression for a node in the DOM tree.
     * @param node in a DOM tree.
     * @param buffer string buffer.
     */
    private static void buildNodeName(Node node, StringBuffer buffer) {
        if (node.getParentNode() == null) {
            return;
        }
        
        buildNodeName(node.getParentNode(), buffer);
        
        if (node.getParentNode() != null
                && node.getParentNode().getParentNode() != null) {
            buffer.append(".");
        }
        
        buffer.append(node.getLocalName());
    }

    /**
     * Serializes a DOM document
     * @param doc
     * @throws CitrusRuntimeException
     * @return serialized XML string
     */
    public static String serialize(Document doc) {
        LSSerializer serializer = null;

        try {
            if (domImpl == null) {
                registry = DOMImplementationRegistry.newInstance();
                domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }

        serializer = domImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("split-cdata-sections", false);
        serializer.getDomConfig().setParameter("format-pretty-print", true);

        return serializer.writeToString(doc);
    }

    /**
     * Pretty prints a XML string.
     * @param doc
     * @throws CitrusRuntimeException
     * @return pretty printed XML string
     */
    public static String prettyPrint(String xml) {
        LSParser parser = null;

        try {
            if (domImpl == null) {
                registry = DOMImplementationRegistry.newInstance();
                domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }

        parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        parser.getDomConfig().setParameter("cdata-sections", true);
        parser.getDomConfig().setParameter("split-cdata-sections", false);

        LSInput input = domImpl.createLSInput();
        input.setStringData(xml);

        Document doc;
        try {
            doc = parser.parse(input);
        } catch (Exception e) {
            return xml;
        }

        return serialize(doc);
    }

    /**
     * Looks up all namespace attribute declarations for the specified node.
     * @param referenceNode XML node to search for namespace declarations.
     * @return Map containing namespace prefix - namespace url pairs.
     */
    public static Map<String, String> lookupNamespaces(Node referenceNode) {
        Map<String, String> namespaces = new HashMap<String, String>();

        if (referenceNode != null && referenceNode.hasAttributes()) {
            for (int i = 0; i < referenceNode.getAttributes().getLength(); i++) {
                Node attribute = referenceNode.getAttributes().item(i);

                if (attribute.getNodeName().startsWith("xmlns")) {
                    namespaces.put(attribute.getNodeName(), attribute.getNodeValue());
                }
            }
        }

        return namespaces;
    }
    
    /**
     * Parse message payload with DOM implementation.
     * @param messagePayload
     * @throws CitrusRuntimeException
     * @return DOM document.
     */
    public static Document parseMessagePayload(String messagePayload) {
        try {
            if(registry == null) {
                registry = DOMImplementationRegistry.newInstance();
                domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            }
    
            LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            parser.getDomConfig().setParameter("cdata-sections", true);
            parser.getDomConfig().setParameter("split-cdata-sections", false);
            parser.getDomConfig().setParameter("validate-if-schema", true);
            
            parser.getDomConfig().setParameter("resource-resolver", new LSResolverImpl(domImpl));
            
            parser.getDomConfig().setParameter("element-content-whitespace", false);
            
            LSInput receivedInput = domImpl.createLSInput();
            receivedInput.setStringData(messagePayload);
            
            return parser.parse(receivedInput);
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException(e);
        } catch (InstantiationException e) {
            throw new CitrusRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new CitrusRuntimeException(e);
        }
    }
}
