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

package org.citrusframework.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import javax.xml.XMLConstants;

import org.citrusframework.CitrusSettings;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.xml.XmlConfigurer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * Class providing several utility methods for XML processing.
 *
 * @author Jan Szczepanski, Christoph Deppisch
 * @since 2006
 *
 */
public final class XMLUtils {

    /** Configurer instance */
    private static XmlConfigurer configurer;

    static {
        configurer = new XmlConfigurer();
        configurer.initialize();
    }

    /**
     * Prevent instantiation.
     */
    private XMLUtils() {
        super();
    }

    /**
     * Initializes XML utilities with custom configurer.
     * @param xmlConfigurer
     */
    public static void initialize(XmlConfigurer xmlConfigurer) {
        configurer = xmlConfigurer;
    }

    /**
     * Creates basic parser instance.
     * @return
     */
    public static LSParser createLSParser() {
        return configurer.createLSParser();
    }

    /**
     * Creates basic serializer instance.
     * @return
     */
    public static LSSerializer createLSSerializer() {
        return configurer.createLSSerializer();
    }

    /**
     * Creates LSInput from dom implementation.
     * @return
     */
    public static LSInput createLSInput() {
        return configurer.createLSInput();
    }

    /**
     * Creates LSOutput from dom implementation.
     * @return
     */
    public static LSOutput createLSOutput() {
        return configurer.createLSOutput();
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
            Node found = findNodeByName(doc, pathExpression.substring(0, pathExpression.length() - attributeName.length() - 1));

            if (found != null) {
                return found.getAttributes().getNamedItem(attributeName);
            } else {
                return null;
            }
        }

        StringBuffer pathName;
        Node parent;
        for (int j=0; j<elements.getLength(); j++) {
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

        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            buildNodeName(((Attr) node).getOwnerElement(), buffer);
            buffer.append(".");
            buffer.append(node.getLocalName());
        } else {
            buildNodeName(node, buffer);
        }

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
        LSSerializer serializer = configurer.createLSSerializer();

        LSOutput output = configurer.createLSOutput();
        String charset = getTargetCharset(doc).displayName();
        output.setEncoding(charset);

        StringWriter writer = new StringWriter();
        output.setCharacterStream(writer);

        serializer.write(doc, output);

        return writer.toString();
    }

    /**
     * Pretty prints a XML string.
     * @param xml
     * @throws CitrusRuntimeException
     * @return pretty printed XML string
     */
    public static String prettyPrint(String xml) {
        LSParser parser = configurer.createLSParser();
        configurer.setParserConfigParameter(parser, XmlConfigurer.VALIDATE_IF_SCHEMA, false);

        LSInput input = configurer.createLSInput();

        try {
            Charset charset = getTargetCharset(xml);

            input.setByteStream(new ByteArrayInputStream(xml.trim().getBytes(charset)));
            input.setEncoding(charset.displayName());
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
        if (referenceNode.getNodeType() == Node.DOCUMENT_NODE) {
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
     * Parse message payload with DOM implementation.
     * @param messagePayload
     * @throws CitrusRuntimeException
     * @return DOM document.
     */
    public static Document parseMessagePayload(String messagePayload) {
        LSParser parser = configurer.createLSParser();
        LSInput receivedInput = configurer.createLSInput();
        try {
            Charset charset = getTargetCharset(messagePayload);
            receivedInput.setByteStream(new ByteArrayInputStream(messagePayload.trim().getBytes(charset)));
            receivedInput.setEncoding(charset.displayName());
        } catch(UnsupportedEncodingException e) {
            throw new CitrusRuntimeException(e);
        }

        return parser.parse(receivedInput);
    }

    /**
     * Try to find encoding for document node. Also supports Citrus default encoding set
     * as System property.
     * @param doc
     * @return
     */
    public static Charset getTargetCharset(Document doc) {
        String defaultEncoding = System.getProperty(CitrusSettings.CITRUS_FILE_ENCODING_PROPERTY,
                System.getenv(CitrusSettings.CITRUS_FILE_ENCODING_ENV));
        if (StringUtils.hasText(defaultEncoding)) {
            return Charset.forName(defaultEncoding);
        }

        if (doc.getInputEncoding() != null) {
            return Charset.forName(doc.getInputEncoding());
        }

        // return as encoding the default UTF-8
        return StandardCharsets.UTF_8;
    }

    /**
     * Try to find target encoding in XML declaration.
     *
     * @param messagePayload XML message payload.
     * @return charsetName if supported.
     */
    private static Charset getTargetCharset(String messagePayload) throws UnsupportedEncodingException {
        String defaultEncoding = System.getProperty(CitrusSettings.CITRUS_FILE_ENCODING_PROPERTY, System.getenv(CitrusSettings.CITRUS_FILE_ENCODING_ENV));
        if (StringUtils.hasText(defaultEncoding)) {
            return Charset.forName(defaultEncoding);
        }

        // trim incoming payload
        String payload = messagePayload.trim();

        char doubleQuote = '\"';
        char singleQuote = '\'';
        // make sure payload has an XML encoding string
        String encodingKey = "encoding";
        if (payload.startsWith("<?xml") && payload.contains(encodingKey) && payload.contains("?>") && (payload.indexOf(encodingKey) < payload.indexOf("?>"))) {

            // extract only encoding part, as otherwise the rest of the complete pay load will be load
            String encoding = payload.substring(payload.indexOf(encodingKey) + encodingKey.length(), payload.indexOf("?>"));

            char quoteChar = doubleQuote;
            int idxDoubleQuote = encoding.indexOf(doubleQuote);
            int idxSingleQuote = encoding.indexOf(singleQuote);

            // check which character is the first one, allowing for <encoding = 'UTF-8'> white spaces
            if (idxSingleQuote >= 0 && (idxDoubleQuote < 0 || idxSingleQuote < idxDoubleQuote)) {
                quoteChar = singleQuote;
            }

            // build encoding using the found character
            encoding = encoding.substring(encoding.indexOf(quoteChar) + 1);
            encoding = encoding.substring(0, encoding.indexOf(quoteChar));

            // check if it has a valid char set
            if (!Charset.availableCharsets().containsKey(encoding)) {
                throw new UnsupportedEncodingException("Found unsupported encoding: '" + encoding + "'");
            }

            // should be a valid encoding
            return Charset.forName(encoding);
        }

        // return as encoding the default UTF-8
        return StandardCharsets.UTF_8;
    }

    /**
     * Removes leading XML declaration from xml if present.
     * @param xml
     * @return
     */
    public static String omitXmlDeclaration(String xml) {
        if (xml.startsWith("<?xml") && xml.contains("?>")) {
            return xml.substring(xml.indexOf("?>") + 2).trim();
        }

        return xml;
    }
}
