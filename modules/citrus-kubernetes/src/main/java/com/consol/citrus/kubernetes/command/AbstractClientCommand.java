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

package com.consol.citrus.kubernetes.command;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractClientCommand<O, R, T extends AbstractClientCommand> extends AbstractKubernetesCommand<R, T> {

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

        if (hasParameter(LABEL)) {
            if (operation instanceof ClientMixedOperation) {
                ((ClientMixedOperation) operation).withLabels(getLabels(getParameters().get(LABEL).toString(), context));
                ((ClientMixedOperation) operation).withoutLabels(getWithoutLabels(getParameters().get(LABEL).toString(), context));
            }

            if (operation instanceof ClientNonNamespaceOperation) {
                ((ClientNonNamespaceOperation) operation).withLabels(getLabels(getParameters().get(LABEL).toString(), context));
                ((ClientNonNamespaceOperation) operation).withoutLabels(getWithoutLabels(getParameters().get(LABEL).toString(), context));
            }
        }

        if (hasParameter(NAMESPACE)) {
            if (operation instanceof ClientMixedOperation) {
                ((ClientMixedOperation) operation).inNamespace(context.replaceDynamicContentInString(getParameters().get(NAMESPACE).toString()));
            }
        }

        if (hasParameter(NAME)) {
            if (operation instanceof ClientMixedOperation) {
                ((ClientMixedOperation) operation).withName(context.replaceDynamicContentInString(getParameters().get(NAME).toString()));
            }

            if (operation instanceof ClientNonNamespaceOperation) {
                ((ClientNonNamespaceOperation) operation).withName(context.replaceDynamicContentInString(getParameters().get(NAME).toString()));
            }
        }

        execute(operation);
    }

    /**
     * Execute the mixed operation
     * @param operation
     */
    protected abstract void execute(O operation);

    /**
     * Subclasses provide operation to call.
     * @param kubernetesClient
     * @param context
     * @return
     */
    protected abstract O operation(KubernetesClient kubernetesClient, TestContext context);

}
