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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class CreateConfigMapAction extends AbstractKubernetesAction implements KubernetesAction {

    private final String configMapName;
    private final List<String> filePaths;
    private final Map<String, String> properties;

    public CreateConfigMapAction(Builder builder) {
        super("create-config-map", builder);

        this.configMapName = builder.configMapName;
        this.filePaths = builder.filePaths;
        this.properties = builder.properties;
    }

    @Override
    public void doExecute(TestContext context) {
        Map<String, String> data = new LinkedHashMap<>();
        for (String filePath : filePaths) {
            try {
                Resource file = FileUtils.getFileResource(filePath, context);
                String resolvedFileContent = context.replaceDynamicContentInString(FileUtils.readToString(file, StandardCharsets.UTF_8));

                data.put(FileUtils.getFileName(file.getLocation()),
                        Base64.getEncoder().encodeToString(resolvedFileContent.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to create config map from filepath", e);
            }
        }

        context.resolveDynamicValuesInMap(properties)
                .forEach((k, v) -> data.put(k, Base64.getEncoder().encodeToString(v.getBytes(StandardCharsets.UTF_8))));

        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(configMapName))
                .endMetadata()
                .withData(data)
                .build();

        getKubernetesClient().configMaps()
                .inNamespace(namespace(context))
                .resource(configMap)
                .createOr(Updatable::update);

        if (isAutoRemoveResources()) {
            context.doFinally(kubernetes().client(getKubernetesClient())
                    .configMaps()
                    .delete(configMapName)
                    .inNamespace(getNamespace()));
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<CreateConfigMapAction, Builder> {

        private String configMapName;
        private final List<String> filePaths = new ArrayList<>();
        private final Map<String, String> properties = new HashMap<>();

        public Builder configMap(String configMapName) {
            this.configMapName = configMapName;
            return this;
        }

        public Builder fromFile(String filePath) {
            this.filePaths.add(filePath);
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties.putAll(properties);
            return this;
        }

        @Override
        public CreateConfigMapAction doBuild() {
            return new CreateConfigMapAction(this);
        }
    }
}
