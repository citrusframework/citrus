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

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import javax.xml.XMLConstants;

import org.springframework.util.StringUtils;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.*;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.xml.LSResolverImpl;

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

    static {
        try {
            registry = DOMImplementationRegistry.newInstance();
            domImpl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }
    
    /**
     * Prevent instantiation.
     */
    private XMLUtils() {
    }
    
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

        checkDomImplInitialization();
        
        serializer = domImpl.createLSSerializer();
        serializer.getDomConfig().setParameter("split-cdata-sections", false);
        serializer.getDomConfig().setParameter("format-pretty-print", true);

        LSOutput output = domImpl.createLSOutput();
        output.setEncoding(doc.getInputEncoding());
        
        StringWriter writer = new StringWriter();
        output.setCharacterStream(writer);
        
        serializer.write(doc, output);
        
        return writer.toString();
    }

    /**
     * Checks if DOM implementation was initialized correctly.
     * @throws CitrusRuntimeException
     */
    private static void checkDomImplInitialization() throws CitrusRuntimeException {
        if (domImpl == null) {
            throw new CitrusRuntimeException("DOM initialization was not done correctly - unable to continue");
        }
    }

    /**
     * Pretty prints a XML string.
     * @param doc
     * @throws CitrusRuntimeException
     * @return pretty printed XML string
     */
    public static String prettyPrint(String xml) {
        LSParser parser = null;

        checkDomImplInitialization();
        
        parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        parser.getDomConfig().setParameter("cdata-sections", true);
        parser.getDomConfig().setParameter("split-cdata-sections", false);

        LSInput input = domImpl.createLSInput();
        try {
            input.setByteStream(new ByteArrayInputStream(xml.trim().getBytes(getTargetCharsetName(xml))));
        } catch(UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        }

        Document doc;
        try {
            doc = parser.parse(input);
        } catch (Exception e) {
            return xml;
        }

        return serialize(doc);
    }

    /**
     * Look up namespace attribute declarations in the specified node and
     * store them in a binding map, where the key is the namespace prefix and the value
     * is the namespace uri.
     * 
     * @param referenceNode XML node to search for namespace declarations.
     * @return map containing namespace prefix - namespace url pairs.
     */
    public static Map<String, String> lookupNamespaces(Node referenceNode) {
        Map<String, String> namespaces = new HashMap<String, String>();

        Node node;
        if(referenceNode.getNodeType() == Node.DOCUMENT_NODE) {
            node = referenceNode.getFirstChild();
        } else {
            node = referenceNode;
        }
        
        if (node != null && node.hasAttributes()) {
            for (int i = 0; i < node.getAttributes().getLength(); i++) {
                Node attribute = node.getAttributes().item(i);

                if (attribute.getNodeName().startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
                    namespaces.put(attribute.getNodeName().substring((XMLConstants.XMLNS_ATTRIBUTE + ":").length()), attribute.getNodeValue());
                } else if (attribute.getNodeName().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) { 
                    //default namespace
                    namespaces.put(XMLConstants.DEFAULT_NS_PREFIX, attribute.getNodeValue());
                }
            }
        }

        return namespaces;
    }
    
    /**
     * Look up namespace attribute declarations in the XML fragment and
     * store them in a binding map, where the key is the namespace prefix and the value
     * is the namespace uri.
     * 
     * @param xmlString XML fragment.
     * @return map containing namespace prefix - namespace uri pairs.
     */
    public static Map<String, String> lookupNamespaces(String xml) {
        Map<String, String> namespaces = new HashMap<String, String>();

        //TODO: handle inner CDATA sections because namespaces they might interfere with real namespaces in xml fragment
        if(xml.indexOf(XMLConstants.XMLNS_ATTRIBUTE) != -1) {
            String[] tokens = StringUtils.split(xml, XMLConstants.XMLNS_ATTRIBUTE);
            
            do {
                String token = tokens[1];

                String nsPrefix;
                if (token.startsWith(":")) {
                    nsPrefix = token.substring(1, token.indexOf("="));
                } else if(token.startsWith("=")) { 
                    nsPrefix = XMLConstants.DEFAULT_NS_PREFIX;
                } else {
                    //we have found a "xmlns" phrase that is no namespace attribute - ignore and continue
                    tokens = StringUtils.split(token, XMLConstants.XMLNS_ATTRIBUTE);
                    continue;
                }

                String nsUri;
                try {
                    nsUri = token.substring(token.indexOf("\"")+1, token.indexOf("\"", token.indexOf("\"")+1));
                } catch (StringIndexOutOfBoundsException e) {
                    //maybe we have more luck with single "'"
                    nsUri = token.substring(token.indexOf("'")+1, token.indexOf("'", token.indexOf("'")+1));
                }
                
                namespaces.put(nsPrefix, nsUri);
                
                tokens = StringUtils.split(token, XMLConstants.XMLNS_ATTRIBUTE);
            } while(tokens != null);
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
        checkDomImplInitialization();
        
        LSParser parser = domImpl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
        parser.getDomConfig().setParameter("cdata-sections", true);
        parser.getDomConfig().setParameter("split-cdata-sections", false);
        parser.getDomConfig().setParameter("validate-if-schema", true);
        
        parser.getDomConfig().setParameter("resource-resolver", new LSResolverImpl(domImpl));
        
        parser.getDomConfig().setParameter("element-content-whitespace", false);
        
        LSInput receivedInput = domImpl.createLSInput();
        try {
            receivedInput.setByteStream(new ByteArrayInputStream(messagePayload.trim().getBytes(getTargetCharsetName(messagePayload))));
        } catch(UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        }
        
        return parser.parse(receivedInput);
    }

    /**
     * Try to find target encoding in XML declaration.
     * 
     * @param messagePayload XML message payload.
     * @return charsetName if supported.
     */
    private static String getTargetCharsetName(String messagePayload) throws UnsupportedEncodingException {
        if(messagePayload.trim().startsWith("<?xml") && messagePayload.contains("encoding")) {
            String encoding = messagePayload.substring(messagePayload.indexOf("encoding") + 8);
            encoding = encoding.substring(encoding.indexOf("\"")+1);
            encoding = encoding.substring(0, encoding.indexOf("\""));
            
            if(Charset.availableCharsets().containsKey(encoding)) {
                return encoding;
            } else {
                throw new UnsupportedEncodingException("Found unsupported encoding: '" + encoding + "'");
            }
        } else {
            return "UTF-8";
        }
    }
}
