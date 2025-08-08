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

package org.citrusframework.knative.actions;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.actions.knative.KnativeActionBuilderBase;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKnativeAction extends AbstractTestAction implements KnativeAction {

    /** Logger */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final KnativeClient knativeClient;
    private final KubernetesClient kubernetesClient;

    private final ClusterType clusterType;
    private final String namespace;

    private final boolean autoRemoveResources;

    private final KnativeActionBuilder knative = new KnativeActionBuilder();

    public AbstractKnativeAction(String name, Builder<?, ?> builder) {
        super("knative:" + name, builder);

        this.knativeClient = builder.knativeClient;
        this.kubernetesClient = builder.kubernetesClient;
        this.namespace = builder.namespace;
        this.clusterType = builder.clusterType;
        this.autoRemoveResources = builder.autoRemoveResources;
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    @Override
    public KnativeClient getKnativeClient() {
        return knativeClient;
    }

    @Override
    public ClusterType clusterType(TestContext context) {
        if (clusterType != null) {
            return clusterType;
        }

        return KnativeAction.super.clusterType(context);
    }

    @Override
    public boolean isAutoRemoveResources() {
        return autoRemoveResources;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public KnativeActionBuilder knative() {
        return knative.client(kubernetesClient)
                .client(knativeClient);
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends KnativeAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B>
            implements ReferenceResolverAware, KnativeActionBuilderBase<T, B> {

        private KnativeClient knativeClient;
        private KubernetesClient kubernetesClient;

        private ClusterType clusterType;
        private String namespace;

        protected ReferenceResolver referenceResolver;

        private boolean autoRemoveResources = KnativeSettings.isAutoRemoveResources();

        /**
         * Use a custom Kubernetes client.
         */
        public B client(KubernetesClient kubernetesClient) {
            this.kubernetesClient = kubernetesClient;
            return self;
        }

        /**
         * Use a custom Knative client.
         */
        public B client(KnativeClient knativeClient) {
            this.knativeClient = knativeClient;
            return self;
        }

        @Override
        public B client(Object o) {
            if (o instanceof KnativeClient client) {
                this.knativeClient = client;
            } else if (o instanceof KubernetesClient client) {
                this.kubernetesClient = client;
            } else {
                throw new CitrusRuntimeException(("Unsupported client type, expected " +
                        "KnativeClient or KubernetesClient, but got: %s").formatted(o.getClass().getName()));
            }

            return self;
        }

        @Override
        public B inNamespace(String namespace) {
            this.namespace = namespace;
            return self;
        }

        @Override
        public B clusterType(ClusterType clusterType) {
            this.clusterType = clusterType;
            return self;
        }

        @Override
        public B autoRemoveResources(boolean enabled) {
            this.autoRemoveResources = enabled;
            return self;
        }

        @Override
        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public T build() {
            if (referenceResolver != null) {
                if (kubernetesClient == null && referenceResolver.isResolvable(KubernetesClient.class)) {
                    kubernetesClient = referenceResolver.resolve(KubernetesClient.class);
                }

                if (knativeClient == null && referenceResolver.isResolvable(KnativeClient.class)) {
                    knativeClient = referenceResolver.resolve(KnativeClient.class);
                }
            }

            return doBuild();
        }

        protected abstract T doBuild();

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }
    }
}
