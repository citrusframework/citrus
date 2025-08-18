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

package org.citrusframework.validation.xml;

import java.util.Map;

import org.citrusframework.validation.context.MessageValidationContextBuilder;
import org.citrusframework.validation.context.ValidationContext;

public interface XmlValidationContextBuilderBase<T extends ValidationContext, B extends XmlValidationContextBuilderBase<T, B>>
        extends MessageValidationContextBuilder<T, B> {

    /**
     * Validates XML namespace with prefix and uri.
     */
    B namespace(String prefix, String namespaceUri);

    /**
     * Validates XML namespace with prefix and uri.
     */
    B namespaces(Map<String, String> namespaces);

    /**
     * Add namespaces as context to the expression evaluation. Keys are prefixes and values are namespace URIs.
     */
    B namespaceContext(String prefix, String namespaceUri);

    /**
     * Add namespaces as context to the expression evaluation. Keys are prefixes and values are namespace URIs.
     */
    B namespaceContext(Map<String, String> namespaces);
}
