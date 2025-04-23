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

package org.citrusframework.validation.context;

import java.util.Set;

/**
 * Basic message validation context holding ignore expressions
 */
public class DefaultMessageValidationContext extends DefaultValidationContext implements MessageValidationContext {

    /** Map holding xpath expressions to identify the ignored message elements */
    private final Set<String> ignoreExpressions;

    /** Should message be validated with its schema definition */
    private final boolean schemaValidation;

    /** Explicit schema repository to use for this validation */
    private final String schemaRepository;

    /** Explicit schema instance to use for this validation */
    private final String schema;

    public DefaultMessageValidationContext() {
        this(new Builder());
    }

    public DefaultMessageValidationContext(MessageValidationContext.Builder<?, ?> builder) {
        this.ignoreExpressions = builder.ignoreExpressions;
        this.schemaValidation = builder.schemaValidation;
        this.schemaRepository = builder.schemaRepository;
        this.schema = builder.schema;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder extends MessageValidationContext.Builder<MessageValidationContext, Builder> {
        @Override
        public MessageValidationContext build() {
            return new DefaultMessageValidationContext(this);
        }
    }

    @Override
    public Set<String> getIgnoreExpressions() {
        return ignoreExpressions;
    }

    @Override
    public boolean isSchemaValidationEnabled() {
        return schemaValidation;
    }

    @Override
    public String getSchemaRepository() {
        return schemaRepository;
    }

    @Override
    public String getSchema() {
        return schema;
    }
}
