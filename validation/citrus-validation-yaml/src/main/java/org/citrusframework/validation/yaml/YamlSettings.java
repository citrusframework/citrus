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

import static java.lang.Boolean.parseBoolean;

public final class YamlSettings {

    private static final String MESSAGE_VALIDATION_STRICT_PROPERTY = "citrus.yaml.message.validation.strict";
    private static final String MESSAGE_VALIDATION_STRICT_ENV = "CITRUS_YAML_MESSAGE_VALIDATION_STRICT";
    private static final String MESSAGE_VALIDATION_STRICT_DEFAULT = "true";

    /**
     * Private constructor prevent instantiation of utility class
     */
    private YamlSettings() {
        // prevent instantiation
    }

    /**
     * Is Yaml message validation strict
     * @return
     */
    public static boolean isStrict() {
        return parseBoolean(
                System.getProperty(MESSAGE_VALIDATION_STRICT_PROPERTY, System.getenv(MESSAGE_VALIDATION_STRICT_ENV) != null ?
                        System.getenv(MESSAGE_VALIDATION_STRICT_ENV) : MESSAGE_VALIDATION_STRICT_DEFAULT));
    }
}
