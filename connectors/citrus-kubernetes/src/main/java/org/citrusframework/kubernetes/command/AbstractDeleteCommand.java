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

import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.StatusDetails;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;

/**
 * @since 2.7
 */
public abstract class AbstractDeleteCommand<T extends HasMetadata, L extends KubernetesResourceList<T>, R extends Resource<T>, C extends KubernetesCommand<T, DeleteResult>> extends AbstractClientCommand<T, DeleteResult, L, R, C> {

    /** Target resource type */
    private final Class<T> type;

    /** Optional resource object to delete */
    private T resource;

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractDeleteCommand(String name, Class<T> type) {
        super("delete-" + name);
        this.type = type;
    }

    @Override
    public void execute(MixedOperation<T, L, R> operation, TestContext context) {
        boolean success;

        if (getParameters().containsKey(KubernetesMessageHeaders.NAME)) {
            List<StatusDetails> results = operation.delete();
            success = !results.isEmpty();
        } else if (resource != null) {
            List<StatusDetails> results = operation.resource(resource).delete();
            success = !results.isEmpty();
        } else {
            KubernetesResourceList<T> items = operation.list();
            if (items.getItems() != null && !items.getItems().isEmpty()) {
                success = operation.delete(items.getItems());
            } else {
                success = true;
            }
        }

        DeleteResult result = new DeleteResult();
        if (resource != null) {
            result.setApVersion(result.getApVersion());
            result.setKind(result.getKind());
        } else {
            result.setKind(type.getSimpleName());
        }

        result.setSuccess(success);
        setCommandResult(new CommandResult<>(result));
    }

    /**
     * Gets the resource.
     *
     * @return
     */
    public T getResource() {
        return resource;
    }

    /**
     * Sets the resource.
     *
     * @param resource
     */
    public void setResource(T resource) {
        this.resource = resource;
    }
}
