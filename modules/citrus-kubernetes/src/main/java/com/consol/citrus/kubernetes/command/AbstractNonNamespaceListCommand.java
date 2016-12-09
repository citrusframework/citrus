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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public abstract class AbstractNonNamespaceListCommand<R> extends AbstractListCommand<R> {

    /** Logger */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Default constructor initializing the command name.
     *
     * @param name
     */
    public AbstractNonNamespaceListCommand(String name) {
        super(name);
    }

    @Override
    public void execute(KubernetesClient kubernetesClient, TestContext context) {
        ClientNonNamespaceOperation operation = listNonNamespaceOperation(kubernetesClient, context);

        if (hasParameter(LABEL)) {
            operation.withLabels(getLabels(getParameters().get(LABEL).toString(), context));
            operation.withoutLabels(getWithoutLabels(getParameters().get(LABEL).toString(), context));
        }

        setCommandResult((R) operation.list());

        if (getCommandResult() != null) {
            log.debug(getCommandResult().toString());
        }
    }

    @Override
    protected final ClientMixedOperation listOperation(KubernetesClient kubernetesClient, TestContext context) {
        throw new UnsupportedOperationException("Non namespace command not allowed to create namespace operation");
    }

    /**
     * Subclasses provide operation to call.
     * @param kubernetesClient
     * @param context
     * @return
     */
    protected abstract ClientNonNamespaceOperation listNonNamespaceOperation(KubernetesClient kubernetesClient, TestContext context);

}
