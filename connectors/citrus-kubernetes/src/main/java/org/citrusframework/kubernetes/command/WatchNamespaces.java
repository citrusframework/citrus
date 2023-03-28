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
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class WatchNamespaces extends AbstractWatchCommand<Namespace, WatchNamespaces> {

    /**
     * Default constructor initializing the command name.
     */
    public WatchNamespaces() {
        super("namespaces");
    }

    @Override
    protected ClientNonNamespaceOperation operation(KubernetesClient kubernetesClient, TestContext context) {
        return kubernetesClient.getClient().namespaces();
    }
}
