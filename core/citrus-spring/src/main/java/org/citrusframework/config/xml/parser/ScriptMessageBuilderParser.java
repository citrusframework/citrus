/*
 * Copyright the original author or authors.
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

package org.citrusframework.config.xml.parser;

import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public interface ScriptMessageBuilderParser {

    /**
     * Parser proper message builder instance form given message element.
     * @param messageElement
     * @return
     */
    DefaultMessageBuilder parse(Element messageElement);

    /**
     * Find and get script builder element from message element.
     * @param messageElement
     * @return
     */
    default Element getBuilderElement(Element messageElement) {
        Element builderElement = DomUtils.getChildElementByTagName(messageElement, "builder");
        if (builderElement == null) {
            throw new IllegalStateException("");
        }

        return builderElement;
    }
}
