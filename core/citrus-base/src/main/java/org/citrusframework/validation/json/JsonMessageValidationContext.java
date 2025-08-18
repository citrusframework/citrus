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

import java.util.Map;

import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;

/**
 * Validation context holding JSON specific validation information.
 * @since 2.3
 */
public class JsonMessageValidationContext extends DefaultMessageValidationContext {

    /**
     * Default constructor.
     */
    public JsonMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public JsonMessageValidationContext(Builder builder) {
        super(builder);
    }

    /**
     * Fluent builder
     */
    public static final class Builder extends MessageValidationContext.Builder<JsonMessageValidationContext, Builder>
            implements JsonMessageValidationContextBuilder<JsonMessageValidationContext, Builder> {

        public static Builder json() {
            return new Builder();
        }

        @Override
        public JsonPathMessageValidationContext.Builder path() {
            return new JsonPathMessageValidationContext.Builder();
        }

        @Override
        public JsonPathMessageValidationContext.Builder expressions(Map<String, Object> expressions) {
            return new JsonPathMessageValidationContext.Builder()
                    .expressions(expressions);
        }

        @Override
        public JsonPathMessageValidationContext.Builder expression(String path, Object expectedValue) {
            return new JsonPathMessageValidationContext.Builder()
                    .expression(path, expectedValue);
        }

        @Override
        public JsonMessageValidationContext build() {
            return new JsonMessageValidationContext(this);
        }
    }
}
