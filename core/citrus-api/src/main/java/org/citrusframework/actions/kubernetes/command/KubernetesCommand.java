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

package org.citrusframework.actions.kubernetes.command;

import java.util.Map;

import org.citrusframework.context.TestContext;

/**
 * Command executes an operation on the kubernetes client (e.g. list, create).
 * Type parameter:
 * <T> the Kubernetes resource that this command operates with (e.g. Pod, Service, Secret, ...)
 * <O> the command output that may be validated (e.g. Pod, PodList, InfoResult, ...)
 * <C> the Kubernetes client implementation
 * @since 2.7
 */
public interface KubernetesCommand<T, R, C> {

    /**
     * Executes command with given kubernetes client and test context.
     */
    void execute(C kubernetesClient, TestContext context);

    /**
     * Gets the Kubernetes command name.
     */
    String getName();

    /**
     * Gets the command parameters.
     */
    Map<String, Object> getParameters();

    /**
     * Adds validation callback with command result.
     */
    KubernetesCommand<T, R, C> validate(KubernetesCommandResultCallback<R> callback);

    /**
     * Provides access to this command result if any.
     */
    KubernetesCommandResult<R> getCommandResult();

    /**
     * Gets the command result callback.
     */
    KubernetesCommandResultCallback<R> getResultCallback();

    /**
     * Sets the label parameter.
     */
    KubernetesCommand<T, R, C> label(String key, String value);

    /**
     * Sets the label parameter.
     */
    KubernetesCommand<T, R, C> label(String key);

    /**
     * Sets the namespace parameter.
     */
    KubernetesCommand<T, R, C> namespace(String key);

    /**
     * Sets the name parameter.
     */
    KubernetesCommand<T, R, C> name(String key);

    /**
     * Sets the without label parameter.
     */
    KubernetesCommand<T, R, C> withoutLabel(String key, String value);

    /**
     * Sets the without label parameter.
     */
    KubernetesCommand<T, R, C> withoutLabel(String key);

    /**
     * Validate command result using the specified command result callback.
     */
    default void validateCommandResult(TestContext context) {
        getResultCallback().validateCommandResult(getCommandResult(), context);
    }
}
