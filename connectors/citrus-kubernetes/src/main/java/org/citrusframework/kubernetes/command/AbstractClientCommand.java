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

import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.util.StringUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractClientCommand<O extends ClientNonNamespaceOperation, R extends KubernetesResource, T extends KubernetesCommand<R>> extends AbstractKubernetesCommand<R, T> {

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
        O operation = operation(kubernetesClient, context);

        if (hasParameter(KubernetesMessageHeaders.LABEL)) {
            operation.withLabels(getLabels(getParameters().get(KubernetesMessageHeaders.LABEL).toString(), context));
            operation.withoutLabels(getWithoutLabels(getParameters().get(KubernetesMessageHeaders.LABEL).toString(), context));
        }

        if (hasParameter(KubernetesMessageHeaders.NAME)) {
            operation = (O) operation.withName(context.replaceDynamicContentInString(getParameters().get(KubernetesMessageHeaders.NAME).toString()));
        }

        if (operation instanceof ClientMixedOperation) {
            if (hasParameter(KubernetesMessageHeaders.NAMESPACE)) {
                operation = (O) ((ClientMixedOperation) operation).inNamespace(context.replaceDynamicContentInString(getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString()));
            } else if (StringUtils.hasText(kubernetesClient.getClient().getNamespace())) {
                operation = (O) ((ClientMixedOperation) operation).inNamespace(kubernetesClient.getClient().getNamespace());
            } else {
                operation = (O) ((ClientMixedOperation) operation).inAnyNamespace();
            }
        }

        execute(operation, context);
    }

    /**
     * Execute the mixed operation
     * @param operation
     * @param context
     */
    protected abstract void execute(O operation, TestContext context);

    /**
     * Subclasses provide operation to call.
     * @param kubernetesClient
     * @param context
     * @return
     */
    protected abstract O operation(KubernetesClient kubernetesClient, TestContext context);

}
