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

package org.citrusframework.kubernetes.command;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.citrusframework.context.TestContext;

/**
 * @since 2.7
 */
public abstract class AbstractGetCommand<T extends HasMetadata, L extends KubernetesResourceList<T>, R extends Resource<T>, C extends KubernetesCommand<T, T>> extends AbstractClientCommand<T, T, L, R, C> {

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractGetCommand(String name) {
        super("get-" + name);
    }

    @Override
    public void execute(MixedOperation<T, L, R> operation, TestContext context) {
        String resourceName = getResourceName(context);
        setCommandResult(new CommandResult<>(operation.withName(resourceName).get()));
    }
}
