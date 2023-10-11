/*
 * Copyright 2006-2017 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientResource;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractDeleteCommand<D extends DeleteResult, R extends KubernetesResource, T extends KubernetesCommand<D>> extends AbstractClientCommand<ClientMixedOperation<R, ? extends KubernetesResourceList, ? extends Doneable<R>, ? extends ClientResource<R, ? extends Doneable<R>>>, D, T> {

    /** Target resource type */
    private Class<R> type;

    /** Optional resource object to create */
    private R resource;

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractDeleteCommand(String name, Class<R> type) {
        super("delete-" + name);
        this.type = type;
    }

    @Override
    public void execute(ClientMixedOperation<R, ? extends KubernetesResourceList, ? extends Doneable<R>, ? extends ClientResource<R, ? extends Doneable<R>>> operation, TestContext context) {
        Boolean success;

        if (getParameters().containsKey(KubernetesMessageHeaders.NAME)) {
            success = operation.delete();
        } else {
            KubernetesResourceList items = operation.list();
            if (items.getItems()!= null && !items.getItems().isEmpty()) {
                success = operation.delete(items.getItems());
            } else {
                success = false;
            }
        }

        D result = (D) new DeleteResult();
        result.setType(type.getSimpleName());
        result.setSuccess(success);

        setCommandResult(new CommandResult<>(result));
    }

    /**
     * Gets the resource.
     *
     * @return
     */
    public R getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     *
     * @param resource
     */
    public void setResource(R resource) {
        this.resource = resource;
    }
}
