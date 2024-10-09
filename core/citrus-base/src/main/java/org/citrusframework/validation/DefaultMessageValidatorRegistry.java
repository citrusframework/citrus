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

package org.citrusframework.validation;

/**
 * Default registry adds default message validators located via resource path lookup. This adds
 * all validators present on the classpath.
 *
 */
public class DefaultMessageValidatorRegistry extends MessageValidatorRegistry {

    /**
     * Default constructor adds message validator implementations from resource path lookup.
     */
    public DefaultMessageValidatorRegistry() {
        MessageValidator.lookup().forEach(this::addMessageValidator);
        SchemaValidator.lookup().forEach(this::addSchemaValidator);
    }
}
