/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.dictionary;

import org.citrusframework.context.TestContext;
import org.citrusframework.variable.dictionary.xml.XpathMappingDataDictionary;
import org.w3c.dom.Node;

/**
 * @author Christoph Deppisch
 */
public class OutboundXmlDataDictionary extends XpathMappingDataDictionary {

    @Override
    public <T> T translate(Node node, T value, TestContext context) {
        if (value instanceof String) {
            String toTranslate;
            if (!mappings.isEmpty()) {
                toTranslate = (String) super.translate(node, value, context);
            } else {
                toTranslate = (String) value;
            }

            if (toTranslate.equals(value)) {
                if (toTranslate.equals("true") || toTranslate.equals("false")) {
                    return (T) toTranslate;
                } else if (Character.isDigit(toTranslate.charAt(0))) {
                    return (T) (context.replaceDynamicContentInString("citrus:randomNumber(" + toTranslate.length() + ")"));
                } else if (toTranslate.startsWith("string")) {
                    return (T) (context.replaceDynamicContentInString("citrus:randomString(" + toTranslate.length() + ")"));
                }
            } else {
                return (T) toTranslate;
            }
        }

        return super.translate(node, value, context);
    }
}
