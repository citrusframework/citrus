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

/**
 * Fluent builder
 *
 * @param <B>
 */
public interface SchemaValidationContextBuilder<B> {

    /**
     * Sets schema validation enabled/disabled for this message.
     */
    B schemaValidation(final boolean enabled);

    /**
     * Sets explicit schema instance name to use for schema validation.
     */
    B schema(final String schemaName);

    /**
     * Sets explicit xsd schema repository instance to use for validation.
     */
    B schemaRepository(final String schemaRepository);
}
