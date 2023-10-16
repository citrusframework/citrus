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

package org.citrusframework.xml.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XPath utility class providing static utility methods
 * dealing with XPath expression evaluation.
 *
 * Class is abstract to prevent instantiation.
 *
 * @author Christoph Deppisch
 */
public abstract class XPathUtils {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XPathUtils.class);

    /** Dynamic namespace prefix suffix */
    public static final String DYNAMIC_NS_START = "{";
    public static final String DYNAMIC_NS_END = "}";

    /** Dynamic namespace prefix */
    private static final String DYNAMIC_NS_PREFIX = "dns";

    /**
     * Prevent instantiation.
     */
    private XPathUtils() {
    }

    /**
     * Extracts dynamic namespaces that are inline inside a XPath expression. Example:
     * <code>/{http://sample.org/foo}foo/{http://sample.org/bar}bar</code>
     * @param expression
     * @return
     */
    public static Map<String, String> getDynamicNamespaces(String expression) {
        Map<String, String> namespaces = new HashMap<String, String>();

        if (expression.contains(DYNAMIC_NS_START) && expression.contains(DYNAMIC_NS_END)) {
            String[] tokens = expression.split("\\" + DYNAMIC_NS_START);

            for (int i = 1; i < tokens.length; i++) {
                String namespace = tokens[i].substring(0, tokens[i].indexOf(DYNAMIC_NS_END));

                if (!namespaces.containsValue(namespace)) {
                    namespaces.put(DYNAMIC_NS_PREFIX + i, namespace);
                }
            }
        }

        return namespaces;
    }

    /**
     * Replaces all dynamic namespaces in a XPath expression with respective prefixes
     * in namespace map.
     *
     * XPath: <code>/{http://sample.org/foo}foo/{http://sample.org/bar}bar</code>
     * results in <code>/ns1:foo/ns2:bar</code> where the namespace map contains ns1 and ns2.
     *
     * @param expression
     * @param namespaces
     * @return
     */
    public static String replaceDynamicNamespaces(String expression, Map<String, String> namespaces) {
        String expressionResult = expression;

        for (Entry<String, String> namespaceEntry : namespaces.entrySet()) {
            if (expressionResult.contains(DYNAMIC_NS_START + namespaceEntry.getValue() + DYNAMIC_NS_END)) {
                expressionResult = expressionResult.replaceAll("\\" + DYNAMIC_NS_START + namespaceEntry.getValue().replace(".", "\\.") + "\\" + DYNAMIC_NS_END,
                        namespaceEntry.getKey() + ":");
            }
        }

        return expressionResult;
    }

    /**
     * Searches for dynamic namespaces in expression.
     * @param expression
     * @return
     */
    public static boolean hasDynamicNamespaces(String expression) {
        return expression.contains(DYNAMIC_NS_START) && expression.contains(DYNAMIC_NS_END);
    }

    /**
     * Evaluate XPath expression as String result type regardless
     * what actual result type the expression will evaluate to.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @param resultType
     * @return
     */
    public static Object evaluate(Node node, String xPathExpression,
            NamespaceContext nsContext, XPathExpressionResult resultType) {
        if (resultType.equals(XPathExpressionResult.NODE)) {
            Node resultNode = evaluateAsNode(node, xPathExpression, nsContext);

            if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                if (resultNode.getFirstChild() != null) {
                    return resultNode.getFirstChild().getNodeValue();
                } else {
                    return "";
                }
            } else {
                return resultNode.getNodeValue();
            }
        } else if (resultType.equals(XPathExpressionResult.NODESET)) {
            NodeList resultNodeList = evaluateAsNodeList(node, xPathExpression, nsContext);

            List<String> values = new ArrayList<>();
            for (int i = 0; i < resultNodeList.getLength(); i++) {
                Node resultNode = resultNodeList.item(i);

                if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                    if (resultNode.getFirstChild() != null) {
                        values.add(resultNode.getFirstChild().getNodeValue());
                    } else {
                        values.add("");
                    }
                } else {
                    values.add(resultNode.getNodeValue());
                }
            }

            return values;
        } else if (resultType.equals(XPathExpressionResult.STRING)) {
            return evaluateAsString(node, xPathExpression, nsContext);
        } else {
            Object result = evaluateAsObject(node, xPathExpression, nsContext, resultType.getAsQName());

            if (result == null) {
                throw new CitrusRuntimeException("No result for XPath expression: '" + xPathExpression + "'");
            }

            if (resultType.equals(XPathExpressionResult.INTEGER)) {
                return (int) Math.round((Double) result);
            }

            return result;
        }
    }

    /**
     * Evaluate XPath expression with result type Node.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static Node evaluateAsNode(Node node, String xPathExpression, NamespaceContext nsContext) {
        Node result = (Node) evaluateExpression(node, xPathExpression, nsContext, XPathConstants.NODE);

        if (result == null) {
            throw new CitrusRuntimeException("No result for XPath expression: '" + xPathExpression + "'");
        }

        return result;
    }

    /**
     * Evaluate XPath expression with result type NodeList.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static NodeList evaluateAsNodeList(Node node, String xPathExpression, NamespaceContext nsContext) {
        NodeList result = (NodeList) evaluateExpression(node, xPathExpression, nsContext, XPathConstants.NODESET);

        if (result == null) {
            throw new CitrusRuntimeException("No result for XPath expression: '" + xPathExpression + "'");
        }

        return result;
    }

    /**
     * Evaluate XPath expression with result type String.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static String evaluateAsString(Node node, String xPathExpression, NamespaceContext nsContext) {
        String result = (String) evaluateExpression(node, xPathExpression, nsContext, XPathConstants.STRING);

        if (!StringUtils.hasText(result)) {
            //result is empty so check if the expression node really exists
            //if node does not exist an exception is thrown
            evaluateAsNode(node, xPathExpression, nsContext);
        }

        return result;
    }

    /**
     * Evaluate XPath expression with result type Boolean value.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static Boolean evaluateAsBoolean(Node node, String xPathExpression, NamespaceContext nsContext) {
        return (Boolean) evaluateExpression(node, xPathExpression, nsContext, XPathConstants.BOOLEAN);
    }

    /**
     * Evaluate XPath expression with result type Number.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static Double evaluateAsNumber(Node node, String xPathExpression, NamespaceContext nsContext) {
        return (Double) evaluateExpression(node, xPathExpression, nsContext, XPathConstants.NUMBER);
    }

    /**
     * Evaluate XPath expression.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     */
    public static Object evaluateAsObject(Node node, String xPathExpression, NamespaceContext nsContext, QName resultType) {
        return evaluateExpression(node, xPathExpression, nsContext, resultType);
    }

    /**
     * Construct a xPath expression instance with given expression string and namespace context.
     * If namespace context is not specified a default context is built from the XML node
     * that is evaluated against.
     * @param xPathExpression
     * @param nsContext
     * @return
     * @throws XPathExpressionException
     */
    private static XPathExpression buildExpression(String xPathExpression, NamespaceContext nsContext)
            throws XPathExpressionException {
        XPath xpath = createXPathFactory().newXPath();

        if (nsContext != null) {
            xpath.setNamespaceContext(nsContext);
        }

        return xpath.compile(xPathExpression);
    }

    /**
     * Method to find out whether an expression is of XPath nature or custom dot notation syntax.
     * @param expression the expression string to check.
     * @return boolean the result.
     */
    public static boolean isXPathExpression(String expression) {
        return expression.indexOf('/') != (-1) || expression.indexOf('(') != (-1);
    }

    /**
     * Evaluates the expression.
     *
     * @param node the node.
     * @param xPathExpression the expression.
     * @param nsContext the context.
     * @param returnType
     * @return the result.
     */
    public static Object evaluateExpression(Node node, String xPathExpression, NamespaceContext nsContext, QName returnType) {
        try {
            return buildExpression(xPathExpression, nsContext).evaluate(node, returnType);
        } catch (XPathExpressionException e) {
            throw new CitrusRuntimeException("Can not evaluate xpath expression '" + xPathExpression + "'", e);
        }
    }

    /**
     * Creates new xpath factory which is not thread safe per definition.
     * @return
     */
    private synchronized static XPathFactory createXPathFactory() {
        XPathFactory factory = null;

        // read system property and see if there is a factory set
        Properties properties = System.getProperties();
        for (Map.Entry<Object, Object> prop : properties.entrySet()) {
            String key = (String) prop.getKey();
            if (key.startsWith(XPathFactory.DEFAULT_PROPERTY_NAME)) {
                String uri = key.indexOf(":") > 0 ? key.substring(key.indexOf(":") + 1) : null;
                if (uri != null) {
                    try {
                        factory = XPathFactory.newInstance(uri);
                    } catch (XPathFactoryConfigurationException e) {
                        logger.warn("Failed to instantiate xpath factory", e);
                        factory = XPathFactory.newInstance();
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created xpath factory {} using system property {} with value {}", factory, key, uri);
                    }
                }
            }
        }

        if (factory == null) {
            factory = XPathFactory.newInstance();
            if (logger.isDebugEnabled()) {
                logger.debug("Created default xpath factory {}", factory);
            }
        }

        return factory;
    }

}
