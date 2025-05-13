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

package org.citrusframework.validation.yaml;

import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;

/**
 * Validation context holding YAML specific validation information.
 * @since 4.6.1
 */
public class YamlMessageValidationContext extends DefaultMessageValidationContext {

    /**
     * Default constructor.
     */
    public YamlMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public YamlMessageValidationContext(Builder builder) {
        super(builder);
    }

    /**
     * Fluent builder
     */
    public static final class Builder extends MessageValidationContext.Builder<YamlMessageValidationContext, Builder> {

        public static Builder yaml() {
            return new Builder();
        }

        @Override
        public YamlMessageValidationContext build() {
            return new YamlMessageValidationContext(this);
        }
    }
}
