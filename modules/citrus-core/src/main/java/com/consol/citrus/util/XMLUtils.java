/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
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
 * Several utillity methods for XML processing.
 *
 * @author js Jan Szczepanski, deppisch Christoph Deppisch Consol*GmbH 2006
 *
 */
public class XMLUtils {
    private static DOMImplementationRegistry registry = null;
    private static DOMImplementationLS domImpl = null;

    /**
     * Searches for a <tt>Node</tt> within a DOM document with the given path name.
     * Elements have to be seperated by <tt>'.'</tt>
     * <br>Example: XML looks like this ('<' and '>' replaced by '[' and ']'):
     * <blockquote><pre>
     * [Foo]
     *   [Bar]
     *     [Poo]text[/Poo]
     *   [/Bar]
     * [/Foo]</pre></blockquote>
     * If you want the <tt>Poo</tt>-Node, the corresponding <tt>sourcePathName</tt>
     * has to be: <tt>Foo.Bar.Poo</tt><p>
     * @see #getNodesPathName(Node)
     * @param doc DOM Document to search for a node.
     * @param sourcePathName Name of the node with complete path name seperated by <tt>'.'</tt> eg: <tt>Foo.Bar.Poo</tt>
     * @return <tt>Node</tt> - if node was found within <tt>doc</tt>, otherwise <tt>null</tt>.
     */
    public static Node findNodeByName(Document doc, String sourcePathName) {
        final StringTokenizer tok = new StringTokenizer(sourcePathName, ".");
        final int numToks = tok.countTokens();
        NodeList elements;
        if (numToks == 1) {
            elements = doc.getElementsByTagNameNS("*", sourcePathName);
            return elements.item(0);
        }

        String element = sourcePathName.substring(sourcePathName.lastIndexOf('.')+1);
        elements = doc.getElementsByTagNameNS("*", element);

        String attributeName = null;
        if (elements.getLength() == 0) {
            //No element found, but maybe we are searching for an attribute
            attributeName = element;

            //cut off attributeName and set element to next token and continue
            sourcePathName = sourcePathName.substring(0, sourcePathName.length()-attributeName.length()-1);
            Node found = findNodeByName(doc, sourcePathName);

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
            if (pathName.toString().equals(sourcePathName)) {return elements.item(j);}
        }

        return null;
    }
    
    public static Node findNodeByXPath(Node node, String expressionStr) {
        return findNodeByXPath(node, expressionStr, null);
    }

    /**
     * Finds a node in the dom tree using XPath
     * @param node the xml node
     * @param xpath the XPath expression
     * @throws CitrusRuntimeException
     * @return the node searched for
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
    
    private static NamespaceContext buildNamespaceContext(Node node) {
        NamespaceContextImpl nsContext = new NamespaceContextImpl();
        
        if(node.getNodeType() == Node.DOCUMENT_NODE) {
            nsContext.setNamespaces(lookupNamespaces(node.getFirstChild()));
        } else {
            nsContext.setNamespaces(lookupNamespaces(node));
        }
        
        return nsContext;
    }

    public static String evaluateXPathExpression(Node node, String expressionStr) {
        return evaluateXPathExpression(node, expressionStr, null);
    }

    /**
     * Evaluates the XPath expression to return the respective value
     * @param node
     * @throws CitrusRuntimeException
     * @return
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
     * Method to find out wheather an expression is XPath or custom
     * @param expression the expression to validate
     * @return boolean the result
     */
    public static boolean isXPathExpression(String expression) {
        return expression.indexOf("/") != (-1) || expression.indexOf("(") != (-1);
    }

    /**
     * Removes <tt>TEXT_NODE</tt>s that are only containing whitspace
     * from a given node and from all of its child nodes.
     *
     * @param element The <tt>Node</tt> to normalize.
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
     * Returns the path name for a given <tt>Node</tt>.
     * <br> Path name looks like: <tt>Foo.Bar.Poo</tt>
     * for the <tt>Poo</tt>-Node in the example XML:
     * <blockquote><pre>
     * [Foo]
     *   [Bar]
     *     [Poo]text[/Poo]
     *   [/Bar]
     * [/Foo]</pre></blockquote>
     *
     * @param node <tt>Node</tt>
     * @return <tt>String</tt> - The path name representation of the node.
     */
    public static String getNodesPathName(Node node) {
        final StringBuffer buffer = new StringBuffer();
        buildNodeName(node, buffer);
        return buffer.toString();
    }

    /**
     * Builds the node name for {@link #getNodesPathName(Node)}
     *
     * @param node
     * @param buffer
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
     * @return serialized xml string
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
     * Serializes a DOM document
     * @param doc
     * @throws CitrusRuntimeException
     * @return serialized xml string
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
     * @param messagePayload
     * @throws CitrusRuntimeException
     * @return
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
