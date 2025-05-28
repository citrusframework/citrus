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

package org.citrusframework.camel.actions.infra;

public final class CamelInfraSettings {

    static final String CAMEL_INFRA_PROPERTY_PREFIX = "citrus.camel.infra.";
    static final String CAMEL_INFRA_ENV_PREFIX = "CITRUS_CAMEL_INFRA_";

    private static final String AUTO_REMOVE_SERVICES_PROPERTY = CAMEL_INFRA_PROPERTY_PREFIX + "auto.remove.services";
    private static final String AUTO_REMOVE_SERVICES_ENV = CAMEL_INFRA_ENV_PREFIX + "AUTO_REMOVE_SERVICES";
    private static final String AUTO_REMOVE_SERVICES_DEFAULT = "true";

    private static final String CAMEL_DUMP_SERVICE_OUTPUT_PROPERTY = CAMEL_INFRA_PROPERTY_PREFIX + "dump.service.output";
    private static final String CAMEL_DUMP_SERVICE_OUTPUT_ENV = CAMEL_INFRA_ENV_PREFIX + "DUMP_SERVICE_OUTPUT";
    private static final String CAMEL_DUMP_SERVICE_OUTPUT_DEFAULT = "false";

    private CamelInfraSettings() {
        //prevent instantiation of utility class
    }

    /**
     * When set to true test will automatically stop and remove Camel infra services that have been started.
     * @return
     */
    public static boolean isAutoRemoveServices() {
        return Boolean.parseBoolean(System.getProperty(AUTO_REMOVE_SERVICES_PROPERTY,
                System.getenv(AUTO_REMOVE_SERVICES_ENV) != null ? System.getenv(AUTO_REMOVE_SERVICES_ENV) : AUTO_REMOVE_SERVICES_DEFAULT));
    }

    /**
     * When set to true infra service output will be redirected to a file in the current working directory.
     * @return
     */
    public static boolean isDumpServiceOutput() {
        return Boolean.parseBoolean(System.getProperty(CAMEL_DUMP_SERVICE_OUTPUT_PROPERTY,
                System.getenv(CAMEL_DUMP_SERVICE_OUTPUT_ENV) != null ? System.getenv(CAMEL_DUMP_SERVICE_OUTPUT_ENV) : CAMEL_DUMP_SERVICE_OUTPUT_DEFAULT));
    }
}
