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

import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.dsl.*;

/**
 * @since 2.7
 */
public class ListNamespaces extends AbstractListCommand<Namespace, NamespaceList, Resource<Namespace>, ListNamespaces> {

    /**
     * Default constructor initializing the command name.
     */
    public ListNamespaces() {
        super("namespaces");
    }

    @Override
    protected MixedOperation<Namespace, NamespaceList, Resource<Namespace>> operation(KubernetesClient kubernetesClient, TestContext context) {
        return (MixedOperation<Namespace, NamespaceList, Resource<Namespace>>) kubernetesClient.getClient().namespaces();
    }

    @Override
    protected boolean isNamespaceOperation() {
        return false;
    }
}
