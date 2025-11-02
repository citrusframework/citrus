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

package org.citrusframework.kubernetes.yaml;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.VerifyCustomResourceAction;
import org.citrusframework.yaml.SchemaProperty;

public class VerifyCustomResource extends AbstractKubernetesAction.Builder<VerifyCustomResourceAction, VerifyCustomResource> {

    private final VerifyCustomResourceAction.Builder delegate = new VerifyCustomResourceAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.resourceName(name);
    }

    @SchemaProperty
    public void setType(String resourceType) {
        try {
            delegate.resourceType(Class.forName(resourceType));
        } catch(ClassNotFoundException | ClassCastException e) {
            delegate.type(resourceType);
        }
    }

    @SchemaProperty
    public void setKind(String kind) {
        delegate.kind(kind);
    }

    @SchemaProperty
    public void setGroup(String group) {
        delegate.group(group);
    }

    @SchemaProperty
    public void setVersion(String version) {
        delegate.version(version);
    }

    @SchemaProperty
    public void setApiVersion(String apiVersion) {
        delegate.apiVersion(apiVersion);
    }

    @SchemaProperty
    public void setCondition(String value) {
        this.delegate.condition(value);
    }

    @SchemaProperty
    public void setLabel(String labelExpression) {
        String[] tokens = labelExpression.split("=", 2);
        this.delegate.label(tokens[0].trim(), tokens[1].trim());
    }

    @SchemaProperty
    public void setMaxAttempts(int maxAttempts) {
        this.delegate.maxAttempts(maxAttempts);
    }

    @SchemaProperty
    public void setDelayBetweenAttempts(long delayBetweenAttempts) {
        this.delegate.delayBetweenAttempts(delayBetweenAttempts);
    }

    @Override
    public VerifyCustomResource description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public VerifyCustomResource actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public VerifyCustomResource client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public VerifyCustomResource inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public VerifyCustomResource autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public VerifyCustomResourceAction doBuild() {
        return delegate.build();
    }
}
