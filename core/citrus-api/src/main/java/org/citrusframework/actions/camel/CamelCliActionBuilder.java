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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface CamelCliActionBuilder<T extends TestAction, B extends CamelCliActionBuilder<T, B>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    B camelVersion(String camelVersion);

    B kameletsVersion(String kameletsVersion);

    /**
     * Runs Camel integration.
     */
    CamelIntegrationRunActionBuilder<?, ?> run();

    /**
     * Runs Camel integration from given source code.
     */
    CamelIntegrationRunActionBuilder<?, ?> run(String name, String sourceCode);

    /**
     * Executes custom commands with Camel CLI.
     */
    CamelIntegrationRunCustomizedActionBuilder<?, ?> custom(String... commands);

    /**
     * Verify that given Camel integration is running.
     */
    CamelIntegrationVerifyActionBuilder<?, ?> verify();

    /**
     * Stop the Camel integration CLI process identified by th given integration name.
     */
    CamelIntegrationStopActionBuilder<?, ?> stop();

    /**
     * Perform actions related to Camel CLI plugins.
     */
    CamelCliPluginActionBuilder<?, ?> plugin();

    /**
     * Perform actions related to Camel CLI Kubernetes plugin.
     */
    CamelCliKubernetesActionBuilder<?, ?> kubernetes();

    /**
     * Perform actions related to Camel CLI commands.
     */
    CamelCliCmdActionBuilder<?, ?> cmd();
}
