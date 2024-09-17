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

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.springframework.util.StringUtils;

/**
 * Test action verifies that given Kubernetes resource matches a given condition (e.g. condition=ready). Raises errors
 * when either the resource is not found or not in expected condition state. Both operations are automatically retried
 * for a given amount of attempts.
 */
public class VerifyCustomResourceAction extends AbstractKubernetesAction {

    private final String resourceName;
    private final String type;
    private final Class<? extends CustomResource<?, ?>> resourceType;
    private final String version;
    private final String kind;
    private final String group;
    private final String labelExpression;
    private final int maxAttempts;
    private final long delayBetweenAttempts;

    private final String condition;

    /**
     * Constructor using given builder.
     * @param builder
     */
    public VerifyCustomResourceAction(Builder builder) {
        super("verify-custom-resource-status", builder);
        this.resourceName = builder.resourceName;
        this.type = builder.type;
        this.resourceType = builder.resourceType;
        this.group = builder.group;
        this.version = builder.version;
        this.kind = builder.kind;
        this.labelExpression = builder.labelExpression;
        this.condition = builder.condition;
        this.maxAttempts = builder.maxAttempts;
        this.delayBetweenAttempts = builder.delayBetweenAttempts;
    }

    @Override
    public void doExecute(TestContext context) {
        verifyResource(
                context.replaceDynamicContentInString(resourceName),
                context.replaceDynamicContentInString(labelExpression),
                context.replaceDynamicContentInString(condition),
                context);
    }

    /**
     * Wait for given pod to be in given state.
     * @param name
     * @param labelExpression
     * @param condition
     * @param context
     * @return
     */
    private void verifyResource(String name, String labelExpression, String condition, TestContext context) {
        for (int i = 0; i < maxAttempts; i++) {
            HasMetadata resource;
            if (name != null && !name.isEmpty()) {
                resource = getResource(name, condition, context);
            } else {
                resource = getResourceFromLabel(labelExpression, condition, context);
            }

            if (resource != null) {
                logger.info(String.format("Verified resource '%s' state '%s'!", getNameOrLabel(name, labelExpression), condition));
                return;
            }

            logger.warn(String.format("Waiting for resource '%s' in state '%s' - retry in %s ms",
                    getNameOrLabel(name, labelExpression), condition, delayBetweenAttempts));
            try {
                Thread.sleep(delayBetweenAttempts);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for resource condition", e);
            }
        }

        throw new ActionTimeoutException((maxAttempts * delayBetweenAttempts),
                new CitrusRuntimeException(String.format("Failed to verify resource '%s' - " +
                        "is not in state '%s' after %d attempts", getNameOrLabel(name, labelExpression), condition, maxAttempts)));
    }

    /**
     * Retrieve resource given state.
     * @param name
     * @param condition
     * @param context
     * @return
     */
    private HasMetadata getResource(String name, String condition, TestContext context) {
        if (resourceType != null) {
            CustomResource<?, ?> resource = getKubernetesClient().resources(resourceType)
                    .inNamespace(namespace(context))
                    .withName(name)
                    .get();

            if (resource.getStatus() != null) {
                return verifyResourceStatus(KubernetesSupport.json().convertValue(resource, Map.class), condition) ? resource : null;
            }
        } else {
            GenericKubernetesResource resource = KubernetesSupport.getResource(getKubernetesClient(), namespace(context),
                    getCrdContext(context), name);

            return verifyResourceStatus(resource.getAdditionalProperties(), condition) ? resource : null;
        }

        return null;
    }

    /**
     * Retrieve pod given state selected by label key and value expression.
     * @param labelExpression
     * @param condition
     * @param context
     * @return
     */
    private HasMetadata getResourceFromLabel(String labelExpression, String condition, TestContext context) {
        if (labelExpression == null || labelExpression.isEmpty()) {
            return null;
        }

        String[] tokens = labelExpression.split("=");
        String labelKey = tokens[0];
        String labelValue = tokens.length > 1 ? tokens[1] : "";

        if (resourceType != null) {
            KubernetesResourceList<? extends CustomResource<?, ?>> resourceList = getKubernetesClient().resources(resourceType)
                    .inNamespace(namespace(context))
                    .withLabel(labelKey, labelValue)
                    .list();

            for (CustomResource<?, ?> listItem : resourceList.getItems()) {
                if (listItem.getStatus() != null) {
                    if (verifyResourceStatus(KubernetesSupport.json().convertValue(listItem, Map.class), condition)) {
                        return listItem;
                    }
                }
            }
        } else {
            GenericKubernetesResourceList resourceList = KubernetesSupport.getResources(getKubernetesClient(),
                    namespace(context),
                    getCrdContext(context), labelKey, labelValue);

            return resourceList.getItems().stream()
                    .filter(resource -> this.verifyResourceStatus(resource.getAdditionalProperties(), condition))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }

    /**
     * Checks resource status with expected condition.
     * @param additionalProperties
     * @param condition
     * @return
     */
    private boolean verifyResourceStatus(Map<String, Object> additionalProperties, String condition) {
        Map<String, Object> status = getAsPropertyMap("status", additionalProperties);
        List<Map<String, Object>> conditions = getAsPropertyList("conditions", status);

        return conditions.stream()
                .anyMatch(propertyMap -> propertyMap.getOrDefault("type", "").equals(condition)
                        && Optional.ofNullable(propertyMap.get("status")).map(b -> Boolean.valueOf(b.toString())).orElse(false));
    }

    /**
     * Build proper custom resource definition context from given type, group, kind and version.
     * @param context
     * @return
     */
    private ResourceDefinitionContext getCrdContext(TestContext context) {
        return KubernetesSupport.crdContext(
                context.replaceDynamicContentInString(type),
                context.replaceDynamicContentInString(group),
                context.replaceDynamicContentInString(kind),
                context.replaceDynamicContentInString(version));
    }

    /**
     * If name is set return as pod name. Else return given label expression.
     * @param name
     * @param labelExpression
     * @return
     */
    private String getNameOrLabel(String name, String labelExpression) {
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            return labelExpression;
        }
    }

    /**
     * Read given property from object and cast to map of properties.
     * @param property
     * @param objectMap
     * @return
     */
    private Map<String, Object> getAsPropertyMap(String property, Map<String, Object> objectMap) {
        if (objectMap != null && objectMap.containsKey(property) && objectMap.get(property) instanceof Map) {
            return (Map<String, Object>) objectMap.get(property);
        }

        return Collections.emptyMap();
    }

    /**
     * Read given property from object map and cast to list of objects.
     * @param property
     * @param objectMap
     * @return
     */
    private List<Map<String, Object>> getAsPropertyList(String property, Map<String, Object> objectMap) {
        if (objectMap.containsKey(property) && objectMap.get(property) instanceof List) {
            return ((List<?>) objectMap.get(property)).stream()
                    .map(item -> (Map<String, Object>) item)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractKubernetesAction.Builder<VerifyCustomResourceAction, Builder> {

        private String resourceName;
        private String labelExpression;

        private int maxAttempts = KubernetesSettings.getMaxAttempts();
        private long delayBetweenAttempts = KubernetesSettings.getDelayBetweenAttempts();

        private String condition = "Ready";

        private String type;
        private Class<? extends CustomResource<?, ?>> resourceType;

        private String version = "v1";
        private String kind;
        private String group;

        public Builder resourceName(String name) {
            if (name.contains("/")) {
                String[] tokens = name.split("/");
                if (kind == null) {
                    kind(StringUtils.capitalize(tokens[0]));
                }

                this.resourceName = tokens.length > 1 ? tokens[1] : "";
            } else {
                this.resourceName = name;
            }

            return this;
        }

        public Builder resourceType(Class<CustomResource<?, ?>> resourceType) {
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
            if (resourceType.contains("/")) {
                String[] tokens = resourceType.split("/");
                this.type = tokens[0];

                if (group == null) {
                    group(type.substring(type.indexOf(".") + 1));
                }

                if (tokens.length > 1) {
                    version(tokens[1]);
                }
            } else {
                this.type = resourceType;
            }

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

        public Builder condition(String value) {
            this.condition = value;
            return this;
        }

        public Builder isAvailable() {
            condition("Available");
            return this;
        }

        public Builder isReady() {
            condition("Ready");
            return this;
        }

        public Builder label(String name, String value) {
            this.labelExpression = String.format("%s=%s", name, value);
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder delayBetweenAttempts(long delayBetweenAttempts) {
            this.delayBetweenAttempts = delayBetweenAttempts;
            return this;
        }

        @Override
        public VerifyCustomResourceAction doBuild() {
            return new VerifyCustomResourceAction(this);
        }
    }
}
