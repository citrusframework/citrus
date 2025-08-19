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

package org.citrusframework.variable.xml;

import java.util.Map;

import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.variable.VariableExtractor;

public interface XpathPayloadVariableExtractorBuilder<T extends VariableExtractor, B extends XpathPayloadVariableExtractorBuilder<T, B>>
        extends VariableExtractor.Builder<T, B>, MessageProcessorAdapter, ValidationContextAdapter {

    /**
     * Adds explicit namespace declaration for later path validation expressions.
     */
    B namespace(String prefix, String namespaceUri);

    /**
     * Sets default namespace declarations on this action builder.
     */
    B namespaces(Map<String, String> namespaceMappings);

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        XpathPayloadVariableExtractorBuilder<?, ?> xpath();

        default XpathPayloadVariableExtractorBuilder<?, ?> fromXpath() {
            return xpath();
        }

    }
}
