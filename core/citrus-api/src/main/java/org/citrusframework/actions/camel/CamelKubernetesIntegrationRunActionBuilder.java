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
import org.citrusframework.spi.Resource;

public interface CamelKubernetesIntegrationRunActionBuilder<T extends TestAction, B extends CamelKubernetesIntegrationRunActionBuilder<T, B>>
        extends CamelJBangActionBuilderBase<T, B> {

    /**
     * Sets the integration name.
     */
    B integrationName(String name);

    /**
     * Export given Camel integration resource.
     */
    B integration(Resource resource);

    /**
     * Define runtime.
     */
    B runtime(String runtime);

    /**
     * Define container image builder type.
     */
    B imageBuilder(String imageBuilder);

    /**
     * Set container image registry.
     */
    B imageRegistry(String imageRegistry);

    /**
     * Set cluster type target.
     */
    B clusterType(String clusterType);

    /**
     * Adds a command build property.
     */
    B withBuildProperty(String property);

    /**
     * Adds command build properties.
     */
    B withBuildProperties(String... properties);

    /**
     * Adds a command property.
     */
    B withProperty(String property);

    /**
     * Adds command properties.
     */
    B withProperties(String... properties);

    /**
     * Adds a command trait.
     */
    B withTrait(String trait);

    /**
     * Adds command traits.
     */
    B withTraits(String... traits);

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

    B verbose(boolean enabled);

    B autoRemove(boolean enabled);

    B waitForRunningState(boolean enabled);
}
