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

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.validation.context.ValidationContext;

public interface XmlMessageValidationContextBuilder<T extends ValidationContext, B extends XmlMessageValidationContextBuilder<T, B>>
        extends XmlValidationContextBuilderBase<T, B>, WithExpressions<XpathMessageValidationContextBuilder<?, ?>> {

    /**
     * Convert to Xpath message validation context builder.
     */
    XpathMessageValidationContextBuilder<?, ?> xpath();

    @Deprecated
    default XpathMessageValidationContextBuilder<?, ?> expressions() {
        return xpath();
    }

    default XpathMessageValidationContextBuilder<?, ?> expressions(Map<String, Object> expressions) {
        return xpath().expressions(expressions);
    }

    default XpathMessageValidationContextBuilder<?, ?> expression(String path, Object expectedValue) {
        return xpath().expression(path, expectedValue);
    }

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        XmlMessageValidationContextBuilder<?, ?> xml();

        default XpathMessageValidationContextBuilder<?, ?> xpath() {
            return xml().xpath();
        }
    }

}
