/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.arquillian;

/**
 * @author Christoph Deppisch
 * @since 2.2
 */
public final class CitrusExtensionConstants {

    /** Arquillian extension configuration qualifier */
    public static final String CITRUS_EXTENSION_QUALIFIER = "citrus";

    /** Basic class must be available in classpath to enable extension capabilities */
    public static final String CITRUS = "com.consol.citrus.Citrus";

    /** Remote configuration properties dynamically added to auxiliary archive */
    public static final String CITRUS_REMOTE_PROPERTIES = "citrus.arquillian.remote.properties";

    /** Citrus extension error message */
    public static final String CITRUS_EXTENSION_ERROR = "Citrus extension error";

    /** Observers of same observable event must be ordered (highest first) */
    public static final int REMOTE_CONFIG_PRECEDENCE = 200;
    public static final int INSTANCE_PRECEDENCE = 100;
    public static final int INSTANCE_REMOTE_PRECEDENCE = 100;

    /**
     * Prevent instantiation.
     */
    private CitrusExtensionConstants() {
    }
}
