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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.citrusframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Helper for parsing message validation elements.
 *
 * @author Martin Maher
 * @since 2.5
 */
public class ValidateMessageParserUtil {

    /**
     * Parses 'validate' element containing nested 'json-path' elements.
     * @param validateElement the validate element to parse
     * @param validateJsonPathExpressions adds the parsed json-path elements to this map
     */
    public static void parseJsonPathElements(Element validateElement, Map<String, Object> validateJsonPathExpressions) {
        List<?> jsonPathElements = DomUtils.getChildElementsByTagName(validateElement, "json-path");
        if (jsonPathElements.size() > 0) {
            for (Iterator<?> jsonPathIterator = jsonPathElements.iterator(); jsonPathIterator.hasNext();) {
                Element jsonPathElement = (Element) jsonPathIterator.next();
                String expression = jsonPathElement.getAttribute("expression");
                if (StringUtils.hasText(expression)) {
                    validateJsonPathExpressions.put(expression, jsonPathElement.getAttribute("value"));
                }
            }
        }
    }

}
