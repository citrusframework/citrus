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

package org.citrusframework.config.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Helper for parsing 'extract' elements containing nested xpath or json variable-extractors.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class VariableExtractorParserUtil {

    public static void parseMessageElement(List<?> messageElements, Map<String, Object> pathMessages) {
        for (Object messageElementObject : messageElements) {
            Element messageElement = (Element) messageElementObject;
            String pathExpression = messageElement.getAttribute("path");

            //construct pathExpression with explicit result-type, like boolean:/TestMessage/Value
            if (messageElement.hasAttribute("result-type")) {
                pathExpression = messageElement.getAttribute("result-type") + ":" + pathExpression;
            }

            pathMessages.put(pathExpression, messageElement.getAttribute("variable"));
        }
    }

    public static void addPayloadVariableExtractors(Element element, List<VariableExtractor> variableExtractors, Map<String, Object> extractFromPath) {
        Map<String, String> namespaces = new HashMap<>();
        if (element != null) {
            Element messageElement = DomUtils.getChildElementByTagName(element, "message");
            if (messageElement != null) {
                List<?> namespaceElements = DomUtils.getChildElementsByTagName(messageElement, "namespace");
                if (namespaceElements.size() > 0) {
                    for (Object namespaceElementObject : namespaceElements) {
                        Element namespaceElement = (Element) namespaceElementObject;
                        namespaces.put(namespaceElement.getAttribute("prefix"), namespaceElement.getAttribute("value"));
                    }
                }
            }
        }

        DelegatingPayloadVariableExtractor payloadVariableExtractor = new DelegatingPayloadVariableExtractor.Builder()
                .expressions(extractFromPath)
                .namespaces(namespaces)
                .build();

        variableExtractors.add(payloadVariableExtractor);
    }

}
