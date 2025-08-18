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

package org.citrusframework.validation.json;

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.validation.context.MessageValidationContextBuilder;
import org.citrusframework.validation.context.ValidationContext;

public interface JsonMessageValidationContextBuilder<T extends ValidationContext, B extends JsonMessageValidationContextBuilder<T, B>>
        extends MessageValidationContextBuilder<T, B>, WithExpressions<JsonPathMessageValidationContextBuilder<?, ?>> {

    JsonPathMessageValidationContextBuilder<?, ?> path();

    @Deprecated
    default JsonPathMessageValidationContextBuilder<?, ?> expressions() {
        return path();
    }

    interface Factory {

        /**
         * Fluent API action building entry method used in Java DSL.
         */
        JsonMessageValidationContextBuilder<?, ?> json();

        default JsonPathMessageValidationContextBuilder<?, ?> jsonPath() {
            return json().path();
        }

    }
}
