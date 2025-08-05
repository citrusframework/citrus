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

package org.citrusframework.actions.docker;

import org.citrusframework.TestAction;

public interface DockerContainerCreateActionBuilder<R, T extends TestAction, B extends DockerContainerCreateActionBuilder<R, T, B>>
        extends DockerActionBuilderBase<R, T, B> {

    /**
     * Sets the image id parameter.
     */
    B image(String id);

    /**
     * Sets the image name parameter.
     */
    B name(String name);

    /**
     * Sets the attach-stderr parameter.
     */
    B attachStdErr(Boolean attachStderr);

    /**
     * Sets the attach-stdin parameter.
     */
    B attachStdIn(Boolean attachStdin);

    /**
     * Sets the attach-stdout parameter.
     */
    B attachStdOut(Boolean attachStdout);

    /**
     * Adds capabilities as command parameter.
     */
    B addCapability(Object... capabilities);

    /**
     * Drops capabilities as command parameter.
     */
    B dropCapability(Object... capabilities);

    /**
     * Sets the domain-name parameter.
     */
    B domainName(String domainName);

    /**
     * Adds commands as command parameter.
     */
    B cmd(String... commands);

    /**
     * Adds environment variables as command parameter.
     */
    B env(String... envVars);

    /**
     * Sets the entrypoint parameter.
     */
    B entryPoint(String entrypoint);

    /**
     * Sets the hostname parameter.
     */
    B hostName(String hostname);

    /**
     * Adds port-specs variables as command parameter.
     */
    B portSpecs(String... portSpecs);

    /**
     * Adds exposed-ports variables as command parameter.
     */
    B exposedPorts(Object... exposedPorts);

    /**
     * Adds explicit port bindings as command parameter.
     */
    B portBindings(Object... portBindings);

    /**
     * Adds volumes variables as command parameter.
     */
    B volumes(Object... volumes);

    /**
     * Sets the working-dir parameter.
     */
    B workingDir(String workingDir);
}
