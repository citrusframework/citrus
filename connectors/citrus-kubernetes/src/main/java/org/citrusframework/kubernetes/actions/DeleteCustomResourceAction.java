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

import java.util.Locale;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSupport;

public class DeleteCustomResourceAction extends AbstractKubernetesAction {

    private final String resourceName;
    private final String type;
    private final Class<? extends HasMetadata> resourceType;
    private final String version;
    private final String kind;
    private final String group;

    public DeleteCustomResourceAction(Builder builder) {
        super("delete-custom-resource", builder);

        this.resourceName = builder.resourceName;
        this.type = builder.type;
        this.resourceType = builder.resourceType;
        this.group = builder.group;
        this.version = builder.version;
        this.kind = builder.kind;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedName = context.replaceDynamicContentInString(resourceName);
        if (resourceType != null) {
            getKubernetesClient().resources(resourceType)
                    .inNamespace(namespace(context))
                    .withName(resolvedName)
                    .delete();
        } else {
            getKubernetesClient().genericKubernetesResources(
                            KubernetesSupport.crdContext(
                                    context.replaceDynamicContentInString(type),
                                    context.replaceDynamicContentInString(group),
                                    context.replaceDynamicContentInString(kind),
                                    context.replaceDynamicContentInString(version)))
                    .inNamespace(namespace(context))
                    .withName(resolvedName)
                    .delete();
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<DeleteCustomResourceAction, Builder> {

        private String resourceName;
        private Class<? extends HasMetadata> resourceType;
        private String type;
        private String version;
        private String kind;
        private String group;

        public Builder resourceName(String name) {
            this.resourceName = name;
            return this;
        }

        public Builder resourceType(Class<? extends HasMetadata> resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder type(Class<? extends CustomResource<?, ?>> resourceType) {
            version(resourceType.getAnnotation(Version.class).value());
            group(resourceType.getAnnotation(Group.class).value());
            kind(resourceType.getSimpleName());
            type(String.format("%ss.%s/%s", kind.toLowerCase(Locale.ENGLISH), group, version));
            this.resourceType = resourceType;
            return this;
        }

        public Builder type(String resourceType) {
            this.type = resourceType;
            return this;
        }

        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            String[] groupAndVersion = apiVersion.split("/");

            group(groupAndVersion[0]);
            version(groupAndVersion[1]);
            return this;
        }

        @Override
        public DeleteCustomResourceAction doBuild() {
            return new DeleteCustomResourceAction(this);
        }
    }
}
