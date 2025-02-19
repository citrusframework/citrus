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

package org.citrusframework.kubernetes.actions;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.kubernetes.KubernetesActor;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract action provides access to the Kubernetes client.
 */
public abstract class AbstractKubernetesAction extends AbstractTestAction implements KubernetesAction {

    /** Logger */
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final KubernetesClient kubernetesClient;

    private final String namespace;

    private final boolean autoRemoveResources;

    public AbstractKubernetesAction(String name, Builder<?, ?> builder) {
        super("k8s:" + name, builder);

        this.kubernetesClient = builder.kubernetesClient;
        this.namespace = builder.namespace;
        this.autoRemoveResources = builder.autoRemoveResources;
        this.setActor(builder.getActor());
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public boolean isAutoRemoveResources() {
        return autoRemoveResources;
    }

    /**
     * Action builder.
     */
    public static abstract class Builder<T extends KubernetesAction, B extends Builder<T, B>> extends AbstractTestActionBuilder<T, B> implements ReferenceResolverAware {

        private KubernetesClient kubernetesClient;
        private String namespace;
        private boolean autoRemoveResources = KubernetesSettings.isAutoRemoveResources();
        private ReferenceResolver referenceResolver;

        /**
         * Use a custom Kubernetes client.
         */
        public B client(KubernetesClient kubernetesClient) {
            this.kubernetesClient = kubernetesClient;
            return self;
        }

        /**
         * Use an explicit namespace.
         */
        public B inNamespace(String namespace) {
            this.namespace = namespace;
            return self;
        }

        public B autoRemoveResources(boolean enabled) {
            this.autoRemoveResources = enabled;
            return self;
        }

        public B withReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
            return self;
        }

        @Override
        public final T build() {
            if (kubernetesClient == null) {
                if (referenceResolver != null && referenceResolver.isResolvable(KubernetesClient.class)) {
                    kubernetesClient = referenceResolver.resolve(KubernetesClient.class);
                } else {
                    kubernetesClient = new KubernetesClientBuilder().build();
                    if (referenceResolver != null) {
                        referenceResolver.bind("kubernetesClient", kubernetesClient);
                    }
                }
            }

            if (getActor() == null && KubernetesSettings.isUseDefaultKubernetesActor()) {
                actor(new KubernetesActor(kubernetesClient));
            }

            return doBuild();
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            this.referenceResolver = referenceResolver;
        }

        protected abstract T doBuild();
    }
}
