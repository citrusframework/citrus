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

package com.consol.citrus.xml.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;

import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Node;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.util.XMLUtils;

/**
 * XPath utility class providing static utility methods
 * dealing with XPath expression evaluation.
 *
 * Class is abstract to prevent instantiation.
 *
 * @author Christoph Deppisch
 */
public abstract class XPathUtils {

    /** XPath expression factory */
    private static XPathFactory xPathFactory;

    static {
        xPathFactory = XPathFactory.newInstance();
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
    public static String evaluate(Node node, String xPathExpression,
            NamespaceContext nsContext, XPathExpressionResult resultType) {
        if(resultType.equals(XPathExpressionResult.NODE)) {
            Node resultNode = evaluateAsNode(node, xPathExpression, nsContext);

            if (resultNode == null) {
                throw new UnknownElementException("No element found for XPath expression: '" + xPathExpression + "'");
            }

            if (resultNode.getNodeType() == Node.ELEMENT_NODE) {
                if (resultNode.getFirstChild() != null) {
                    return resultNode.getFirstChild().getNodeValue();
                } else {
                    return "";
                }
            } else {
                return resultNode.getNodeValue();
            }
        } else if(resultType.equals(XPathExpressionResult.STRING)){
            return evaluateAsString(node, xPathExpression, nsContext);
        } else {
            Object result = evaluateAsObject(node, xPathExpression, nsContext, resultType.getAsQName());

            if(result == null) {
                throw new CitrusRuntimeException("No result for XPath expression: '" + xPathExpression + "'");
            } else {
                return result.toString();
            }
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
     * Construct a xPath expression insdtance with given expression string and namespace context.
     * If namespace context is not specified a default context is built from the XML node
     * that is evaluated against.
     * @param node
     * @param xPathExpression
     * @param nsContext
     * @return
     * @throws XPathExpressionException
     */
    private static XPathExpression buildExpression(Node node, String xPathExpression, NamespaceContext nsContext)
            throws XPathExpressionException {
        XPath xpath = xPathFactory.newXPath();

        if(nsContext != null) {
            xpath.setNamespaceContext(nsContext);
        } else {
            xpath.setNamespaceContext(buildNamespaceContext(node));
        }

        return xpath.compile(xPathExpression);
    }

    /**
     * Build a namespace context from a node element. Method searches for all
     * namespace attributes in the node element and binds them to a namespace context.
     *
     * @param node holding namespace declarations.
     * @return the namespace context.
     */
    private static NamespaceContext buildNamespaceContext(Node node) {
        SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
        nsContext.setBindings(XMLUtils.lookupNamespaces(node));

        return nsContext;
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
    private static Object evaluateExpression(Node node, String xPathExpression, NamespaceContext nsContext, QName returnType) {
        try {
            return buildExpression(node, xPathExpression, nsContext).evaluate(node, returnType);
        } catch (XPathExpressionException e) {
            throw new CitrusRuntimeException("Can not evaluate xpath expression '"+xPathExpression+"'", e);
        }
    }

}
