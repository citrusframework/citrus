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
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.util.StringUtils;

/**
 * @since 2.7
 */
public abstract class AbstractClientCommand<T extends HasMetadata, O, L extends KubernetesResourceList<T>, R extends Resource<T>, C extends KubernetesCommand<T, O>> extends AbstractKubernetesCommand<T, O, C> {

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractClientCommand(String name) {
        super(name);
    }

    @Override
    public final void execute(KubernetesClient kubernetesClient, TestContext context) {
        MixedOperation<T, L, R> operation = operation(kubernetesClient, context);

        if (hasParameter(KubernetesMessageHeaders.LABEL)) {
            operation.withLabels(getLabels(getParameters().get(KubernetesMessageHeaders.LABEL).toString(), context));
            operation.withoutLabels(getWithoutLabels(getParameters().get(KubernetesMessageHeaders.LABEL).toString(), context));
        }

        if (operation != null && isNamespaceOperation()) {
            if (hasParameter(KubernetesMessageHeaders.NAMESPACE)) {
                operation = (MixedOperation<T, L, R>) operation.inNamespace(context.replaceDynamicContentInString(getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString()));
            } else if (StringUtils.hasText(kubernetesClient.getClient().getNamespace())) {
                operation = (MixedOperation<T, L, R>) operation.inNamespace(kubernetesClient.getClient().getNamespace());
            } else {
                operation = (MixedOperation<T, L, R>) operation.inAnyNamespace();
            }
        }

        execute(operation, context);
    }

    /**
     * Indicates that the command is supposed to be a namespaced operation.
     * Non namespace operations are listing namespaces or nodes for instance.
     * @return
     */
    protected boolean isNamespaceOperation() {
        return true;
    }

    protected String getResourceName(TestContext context) {
        if (hasParameter(KubernetesMessageHeaders.NAME)) {
            return context.replaceDynamicContentInString(getParameters().get(KubernetesMessageHeaders.NAME).toString());
        }

        return null;
    }

    /**
     * Execute the mixed operation
     * @param operation
     * @param context
     */
    protected abstract void execute(MixedOperation<T, L, R> operation, TestContext context);

    /**
     * Subclasses provide operation to call.
     * @param kubernetesClient
     * @param context
     * @return
     */
    protected abstract MixedOperation<T, L, R> operation(KubernetesClient kubernetesClient, TestContext context);

}
