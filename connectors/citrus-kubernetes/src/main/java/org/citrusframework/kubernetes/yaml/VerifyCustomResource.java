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
import org.citrusframework.util.ClassLoaderHelper;
import org.citrusframework.api.yaml.SchemaProperty;

public class VerifyCustomResource extends AbstractKubernetesAction.Builder<VerifyCustomResourceAction, VerifyCustomResource> {

    private final VerifyCustomResourceAction.Builder delegate = new VerifyCustomResourceAction.Builder();

    @SchemaProperty(description = "Name of the custom resource to verify.")
    public void setName(String name) {
        this.delegate.resourceName(name);
    }

    @SchemaProperty(description = "Custom resource Java class or resource type used to identify the CRD.")
    public void setType(String resourceType) {
        try {
            delegate.resourceType(Class.forName(resourceType, true, ClassLoaderHelper.getClassLoader()));
        } catch(ClassNotFoundException | ClassCastException e) {
            delegate.type(resourceType);
        }
    }

    @SchemaProperty(description = "Kind of the custom resource.")
    public void setKind(String kind) {
        delegate.kind(kind);
    }

    @SchemaProperty(description = "API group of the custom resource.")
    public void setGroup(String group) {
        delegate.group(group);
    }

    @SchemaProperty(description = "API version of the custom resource.")
    public void setVersion(String version) {
        delegate.version(version);
    }

    @SchemaProperty(description = "API group and version of the custom resource, for example citrus.dev/v1.")
    public void setApiVersion(String apiVersion) {
        delegate.apiVersion(apiVersion);
    }

    @SchemaProperty(description = "Status condition type to wait for, such as Ready or Available.")
    public void setCondition(String value) {
        this.delegate.condition(value);
    }

    @SchemaProperty(description = "Label expression in name=value form used to select the resource.")
    public void setLabel(String labelExpression) {
        String[] tokens = labelExpression.split("=", 2);
        this.delegate.label(tokens[0].trim(), tokens[1].trim());
    }

    @SchemaProperty(description = "Maximum number of verification attempts.")
    public void setMaxAttempts(int maxAttempts) {
        this.delegate.maxAttempts(maxAttempts);
    }

    @SchemaProperty(description = "Delay in milliseconds between verification attempts.")
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
