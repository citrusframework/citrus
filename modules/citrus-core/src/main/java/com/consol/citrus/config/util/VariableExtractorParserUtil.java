/*
 * Copyright 2006-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.config.util;

import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper for parsing 'extract' elements containing nested xpath or json variable-extractors.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class VariableExtractorParserUtil {

    public static void parseMessageElement(List<?> messageElements, Map<String, String> xpathMessages, Map<String, String> jsonMessages) {
        for (Iterator<?> iter = messageElements.iterator(); iter.hasNext(); ) {
            Element messageValue = (Element) iter.next();
            String pathExpression = messageValue.getAttribute("path");

            //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
            if (messageValue.hasAttribute("result-type")) {
                pathExpression = messageValue.getAttribute("result-type") + ":" + pathExpression;
            }

            if (JsonPathMessageValidationContext.isJsonPathExpression(pathExpression)) {
                jsonMessages.put(pathExpression, messageValue.getAttribute("variable"));
            } else {
                xpathMessages.put(pathExpression, messageValue.getAttribute("variable"));
            }
        }
    }

    public static void addXpathVariableExtractors(Element element, List<VariableExtractor> variableExtractors, Map<String, String> extractXpath) {
        XpathPayloadVariableExtractor payloadVariableExtractor = new XpathPayloadVariableExtractor();
        payloadVariableExtractor.setXpathExpressions(extractXpath);

        Map<String, String> namespaces = new HashMap<>();
        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
            if (namespaceElements.size() > 0) {
                for (Iterator<?> iter = namespaceElements.iterator(); iter.hasNext(); ) {
                    Element namespaceElement = (Element) iter.next();
                    namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                }
                payloadVariableExtractor.setNamespaces(namespaces);
            }
        }

        variableExtractors.add(payloadVariableExtractor);
    }

    public static void addJsonVariableExtractors(List<VariableExtractor> variableExtractors, Map<String, String> extractJsonPath) {
        JsonPathVariableExtractor payloadVariableExtractor = new JsonPathVariableExtractor();
        payloadVariableExtractor.setJsonPathExpressions(extractJsonPath);

        variableExtractors.add(payloadVariableExtractor);
    }

}
