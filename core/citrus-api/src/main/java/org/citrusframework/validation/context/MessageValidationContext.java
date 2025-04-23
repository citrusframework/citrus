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

import java.util.HashSet;
import java.util.Set;

public interface MessageValidationContext extends ValidationContext, SchemaValidationContext {

    /**
     * Get ignored message elements.
     * @return the ignoreExpressions
     */
    Set<String> getIgnoreExpressions();

    /**
     * Base fluent builder for message validation contexts.
     */
    abstract class Builder<T extends MessageValidationContext, S extends Builder<T, S>>
            implements ValidationContext.Builder<T, Builder<T, S>>, SchemaValidationContext.Builder<Builder<T, S>> {

        protected final S self;

        protected final Set<String> ignoreExpressions = new HashSet<>();
        protected boolean schemaValidation = true;
        protected String schemaRepository;
        protected String schema;

        protected Builder() {
            this.self = (S) this;
        }

        /**
         * Sets schema validation enabled/disabled for this message.
         *
         * @param enabled
         * @return
         */
        public S schemaValidation(final boolean enabled) {
            this.schemaValidation = enabled;
            return self;
        }

        /**
         * Sets explicit schema instance name to use for schema validation.
         *
         * @param schemaName
         * @return
         */
        public S schema(final String schemaName) {
            this.schema = schemaName;
            return self;
        }

        /**
         * Sets explicit xsd schema repository instance to use for validation.
         *
         * @param schemaRepository
         * @return
         */
        public S schemaRepository(final String schemaRepository) {
            this.schemaRepository = schemaRepository;
            return self;
        }

        /**
         * Adds ignore path expression for message element.
         *
         * @param path
         * @return
         */
        public S ignore(final String path) {
            this.ignoreExpressions.add(path);
            return self;
        }

        /**
         * Adds a list of ignore path expressions for message element.
         *
         * @param paths
         * @return
         */
        public S ignore(final Set<String> paths) {
            this.ignoreExpressions.addAll(paths);
            return self;
        }
    }
}
