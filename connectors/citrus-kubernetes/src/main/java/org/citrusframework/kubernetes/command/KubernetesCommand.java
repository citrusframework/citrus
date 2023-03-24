/*
 * Copyright 2006-2016 the original author or authors.
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
import org.citrusframework.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Map;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public interface KubernetesCommand<R extends KubernetesResource> {

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
    KubernetesCommand<R> validate(CommandResultCallback<R> callback);

    /**
     * Provides access to this command result if any.
     * @return
     */
    CommandResult<R> getCommandResult();

    /**
     * Gets the command result callback.
     * @return
     */
    CommandResultCallback<R> getResultCallback();

    /**
     * Sets the label parameter.
     * @param key
     * @param value
     * @return
     */
    KubernetesCommand<R> label(String key, String value);

    /**
     * Sets the label parameter.
     * @param key
     * @return
     */
    KubernetesCommand<R> label(String key);

    /**
     * Sets the namespace parameter.
     * @param key
     * @return
     */
    KubernetesCommand<R> namespace(String key);

    /**
     * Sets the name parameter.
     * @param key
     * @return
     */
    KubernetesCommand<R> name(String key);

    /**
     * Sets the without label parameter.
     * @param key
     * @param value
     * @return
     */
    KubernetesCommand<R> withoutLabel(String key, String value);

    /**
     * Sets the without label parameter.
     * @param key
     * @return
     */
    KubernetesCommand<R> withoutLabel(String key);
}
