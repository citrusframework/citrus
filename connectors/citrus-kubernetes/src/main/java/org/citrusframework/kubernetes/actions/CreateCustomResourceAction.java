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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.dsl.Updatable;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.citrusframework.actions.kubernetes.KubernetesCustomResourceCreateActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;

public class CreateCustomResourceAction extends AbstractKubernetesAction implements KubernetesAction {

    private final String type;
    private final Class<? extends HasMetadata> resourceType;
    private final String version;
    private final String kind;
    private final String group;
    private final String content;
    private final String filePath;

    public CreateCustomResourceAction(Builder builder) {
        super("create-custom-resource", builder);

        this.type = builder.type;
        this.resourceType = builder.resourceType;
        this.group = builder.group;
        this.version = builder.version;
        this.kind = builder.kind;
        this.content = builder.content;
        this.filePath = builder.filePath;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedResource;
        if (filePath != null) {
            try {
                resolvedResource = FileUtils.readToString(Resources.create(context.replaceDynamicContentInString(filePath)));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to read custom resource file", e);
            }
        } else {
            resolvedResource = context.replaceDynamicContentInString(content);
        }

        if (resourceType != null) {
            HasMetadata resource = getKubernetesClient()
                    .resources(resourceType)
                    .inNamespace(namespace(context))
                    .load(new ByteArrayInputStream(resolvedResource.getBytes(StandardCharsets.UTF_8)))
                    .createOr(Updatable::update);

            if (isAutoRemoveResources()) {
                context.doFinally(kubernetes()
                        .customResources()
                        .delete(resource.getMetadata().getName())
                        .inNamespace(getNamespace())
                        .resourceType(resourceType));
            }
        } else {
            GenericKubernetesResource resource = getKubernetesClient()
                    .genericKubernetesResources(KubernetesSupport.crdContext(context.replaceDynamicContentInString(type),
                            context.replaceDynamicContentInString(group),
                            context.replaceDynamicContentInString(kind),
                            context.replaceDynamicContentInString(version)))
                    .inNamespace(namespace(context))
                    .load(new ByteArrayInputStream(resolvedResource.getBytes(StandardCharsets.UTF_8)))
                    .createOr(Updatable::update);

            if (resource.get("messages") != null) {
                throw new CitrusRuntimeException(String.format("Failed to create custom resource - %s", resource.get("messages")));
            }

            if (isAutoRemoveResources()) {
                context.doFinally(kubernetes()
                        .customResources()
                        .delete(resource.getMetadata().getName())
                        .inNamespace(getNamespace())
                        .type(type)
                        .group(group)
                        .kind(kind)
                        .version(version));
            }
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<CreateCustomResourceAction, Builder>
            implements KubernetesCustomResourceCreateActionBuilder<CreateCustomResourceAction, Builder> {

        private String type;
        private Class<? extends HasMetadata> resourceType;
        private String version;
        private String kind;
        private String group;
        private String content;
        private String filePath;

        @Override
        public Builder type(String resourceType) {
            this.type = resourceType;
            return this;
        }

        @Override
        public Builder content(String content) {
            this.content = content;
            return this;
        }

        @Override
        public Builder kind(String kind) {
            this.kind = kind;
            return this;
        }

        @Override
        public Builder group(String group) {
            this.group = group;
            return this;
        }

        @Override
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        @Override
        public Builder apiVersion(String apiVersion) {
            String[] groupAndVersion = apiVersion.split("/");

            group(groupAndVersion[0]);
            version(groupAndVersion[1]);
            return this;
        }

        @Override
        public Builder resourceType(Class<?> resourceType) {
            if (HasMetadata.class.isAssignableFrom(resourceType)) {
                this.resourceType = (Class<? extends HasMetadata>) resourceType;
            } else {
                throw new ClassCastException("Resource type '%s' is not supported".formatted(resourceType.getName()));
            }

            return this;
        }

        @Override
        public Builder file(String filePath) {
            this.filePath = filePath;
            return this;
        }

        @Override
        public Builder resource(Object o) {
            if (o instanceof HasMetadata resource) {

                if (resource.getApiVersion() != null) {
                    apiVersion(resource.getApiVersion());
                } else {
                    version(resource.getClass().getAnnotation(Version.class).value());
                }

                if (resource.getKind() != null) {
                    kind(resource.getKind());
                } else {
                    kind(resource.getClass().getSimpleName());
                }

                if (HasMetadata.getGroup(resource.getClass()) != null) {
                    group(HasMetadata.getGroup(resource.getClass()));
                } else {
                    group(resource.getClass().getAnnotation(Group.class).value());
                }

                type(String.format("%ss.%s/%s", kind.toLowerCase(Locale.ENGLISH), group, version));
                content(KubernetesSupport.dumpYaml(resource));

                this.resourceType = resource.getClass();
            } else {
                throw new CitrusRuntimeException("Invalid resource type %s".formatted(o.getClass().getName()));
            }

            return this;
        }

        @Override
        public CreateCustomResourceAction doBuild() {
            return new CreateCustomResourceAction(this);
        }
    }
}
