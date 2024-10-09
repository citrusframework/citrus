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

import java.util.Map;

import io.fabric8.kubernetes.api.model.HasMetadata;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.client.KubernetesClient;

/**
 * Command executes an operation on the kubernetes client (e.g. list, create).
 * Type parameter:
 * <T> the Kubernetes resource that this command operates with (e.g. Pod, Service, Secret, ...)
 * <O> the command output that may be validated (e.g. Pod, PodList, InfoResult, ...)
 * @since 2.7
 */
public interface KubernetesCommand<T extends HasMetadata, O> {

    /**
     * Executes command with given kubernetes client and test context.
     * @param kubernetesClient
     * @param context
     */
    void execute(KubernetesClient kubernetesClient, TestContext context);

    /**
     * Gets the Kubernetes command name.
     * @return
     */
    String getName();

    /**
     * Gets the command parameters.
     * @return
     */
    Map<String, Object> getParameters();

    /**
     * Adds validation callback with command result.
     * @param callback
     * @return
     */
    KubernetesCommand<T, O> validate(CommandResultCallback<O> callback);

    /**
     * Provides access to this command result if any.
     * @return
     */
    CommandResult<O> getCommandResult();

    /**
     * Gets the command result callback.
     * @return
     */
    CommandResultCallback<O> getResultCallback();

    /**
     * Sets the label parameter.
     * @param key
     * @param value
     * @return
     */
    KubernetesCommand<T, O> label(String key, String value);

    /**
     * Sets the label parameter.
     * @param key
     * @return
     */
    KubernetesCommand<T, O> label(String key);

    /**
     * Sets the namespace parameter.
     * @param key
     * @return
     */
    KubernetesCommand<T, O> namespace(String key);

    /**
     * Sets the name parameter.
     * @param key
     * @return
     */
    KubernetesCommand<T, O> name(String key);

    /**
     * Sets the without label parameter.
     * @param key
     * @param value
     * @return
     */
    KubernetesCommand<T, O> withoutLabel(String key, String value);

    /**
     * Sets the without label parameter.
     * @param key
     * @return
     */
    KubernetesCommand<T, O> withoutLabel(String key);

    /**
     * Validate command result using the specified command result callback.
     * @param context
     */
    default void validateCommandResult(TestContext context) {
        getResultCallback().validateCommandResult(getCommandResult(), context);
    }
}
