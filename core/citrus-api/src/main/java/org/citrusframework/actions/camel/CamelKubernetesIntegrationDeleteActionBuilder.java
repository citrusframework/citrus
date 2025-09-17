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

public interface CamelKubernetesIntegrationDeleteActionBuilder<T extends TestAction, B extends CamelKubernetesIntegrationDeleteActionBuilder<T, B>>
        extends CamelJBangActionBuilderBase<T, B> {

    /**
     * Delete Camel JBang kubernetes resources integration resource.
     */
    B integration(Resource resource);

    /**
     * Delete Camel JBang kubernetes resources for this integration.
     */
    B integration(String name);

    /**
     * Set cluster type target.
     */
    B clusterType(String clusterType);

    /**
     * The working directory where to find exported project sources.
     */
    B workingDir(String dir);

    /**
     * The Namespace where the kubernetes resources are deployed.
     */
    B namespace(String namespace);
}
