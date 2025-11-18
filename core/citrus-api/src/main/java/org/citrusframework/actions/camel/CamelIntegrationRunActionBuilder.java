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

package org.citrusframework.actions.camel;

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.spi.Resource;

public interface CamelIntegrationRunActionBuilder<T extends TestAction, B extends CamelIntegrationRunActionBuilder<T, B>>
        extends CamelJBangActionBuilderBase<T, B> {

    /**
     * Runs Camel integration from given source code.
     */
    B integration(String sourceCode);

    /**
     * Runs given Camel integration resource.
     */
    B integration(Resource resource);

    /**
     * Add resource file to the integration run.
     */
    B addResource(Resource resource);

    /**
     * Construct resource from given path and add file as resource to the integration run.
     */
    B addResource(String resourcePath);

    /**
     * Adds route using one of the supported languages XML or Groovy.
     */
    B integration(String name, String sourceCode);

    /**
     * Sets the integration name.
     */
    B integrationName(String name);

    /**
     * Adds a command argument.
     */
    B withArg(String arg);

    /**
     * Adds a command argument with name and value.
     */
    B withArg(String name, String value);

    /**
     * Adds command arguments.
     */
    B withArgs(String... args);

    /**
     * Adds an environment variable.
     */
    B withEnv(String key, String value);

    /**
     * Adds environment variables.
     */
    B withEnvs(Map<String, String> envVars);

    /**
     * Adds environment variables from given file resource.
     */
    B withEnvs(Resource envVarsFile);

    /**
     * Adds a system properties.
     */
    B withSystemProperty(String key, String value);

    /**
     * Adds system properties.
     */
    B withSystemProperties(Map<String, String> systemProperties);

    /**
     * Adds system properties from given file resource.
     */
    B withSystemProperties(Resource systemPropertiesFile);

    B dumpIntegrationOutput(boolean enabled);

    B autoRemove(boolean enabled);

    B waitForRunningState(boolean enabled);

    B stub(String... components);
}
