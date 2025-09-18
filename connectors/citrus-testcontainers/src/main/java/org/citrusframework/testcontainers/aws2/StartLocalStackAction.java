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

package org.citrusframework.testcontainers.aws2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.citrusframework.actions.testcontainers.TestcontainersLocalStackStartActionBuilder;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.Wait;

public class StartLocalStackAction extends StartTestcontainersAction<LocalStackContainer> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StartLocalStackAction.class);

    private final boolean autoCreateClients;

    private final Map<String, String> options;

    public StartLocalStackAction(Builder builder) {
        super(builder);
        this.autoCreateClients = builder.autoCreateClients;
        this.options = builder.options;
    }

    @Override
    protected void exposeConnectionSettings(LocalStackContainer container, TestContext context) {
        LocalStackSettings.exposeConnectionSettings(container, serviceName, context);

        if (autoCreateClients) {
            for (AwsService service : container.getServices()) {
                String clientName = "%sClient".formatted(service.getServiceName());
                clientName = options.getOrDefault(clientName + "Name", clientName);

                if (context.getReferenceResolver().isResolvable(clientName)) {
                    logger.debug("Client {} already exists - do not overwrite", clientName);
                    // client bean with same name already exists - do not overwrite
                    continue;
                }

                Optional<ClientFactory<?>> clientFactory = ClientFactory.lookup(context.getReferenceResolver(), service);
                if (clientFactory.isPresent()) {
                    Object client = clientFactory.get().createClient(container, context.resolveDynamicValuesInMap(options));
                    PropertyUtils.configure(clientName, client, context.getReferenceResolver());
                    container.addClient(service, client);
                    logger.debug("Auto create client {} for service {}", clientName, service.name());
                    context.getReferenceResolver().bind(clientName, client);
                } else {
                    logger.warn("Missing client factory for service '{}' - no client created for this service", service.name());
                }
            }
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<LocalStackContainer, StartLocalStackAction, Builder>
            implements TestcontainersLocalStackStartActionBuilder<LocalStackContainer, StartLocalStackAction, Builder> {

        private String localStackVersion = LocalStackSettings.getVersion();

        private final Set<AwsService> services = new HashSet<>();

        private boolean autoCreateClients = LocalStackSettings.isAutoCreateClients();

        private final Map<String, String> options = new HashMap<>();

        public Builder() {
            withStartupTimeout(LocalStackSettings.getStartupTimeout());
        }

        @Override
        public Builder version(String localStackVersion) {
           this.localStackVersion = localStackVersion;
           return this;
        }

        @Override
        public Builder withService(AwsService service) {
           this.services.add(service);
           return this;
        }

        @Override
        public Builder withServices(AwsService[] services) {
            this.services.addAll(Arrays.asList(services));
           return this;
        }

        @Override
        public Builder withServices(Set<AwsService> services) {
            this.services.addAll(services);
           return this;
        }

        @Override
        public Builder withOptions(Map<String, String> options) {
            this.options.putAll(options);
            return this;
        }

        @Override
        public Builder withOption(String key, String value) {
            this.options.put(key, value);
            return this;
        }

        @Override
        public Builder autoCreateClients(boolean enabled) {
            this.autoCreateClients = enabled;
            return this;
        }

        @Override
        protected void prepareBuild() {
            if (containerName == null) {
                containerName(LocalStackSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(LocalStackSettings.getServiceName());
            }

            if (image == null) {
                image(LocalStackSettings.getImageName());
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "build");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            LocalStackContainer localStack;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, LocalStackContainer.class)) {
                localStack = referenceResolver.resolve(containerName, LocalStackContainer.class);

                if  (!localStack.isRunning()) {
                    localStack.withNewServices(services.toArray(AwsService[]::new));
                }
            } else {
                localStack = new LocalStackContainer(image, localStackVersion)
                        .withServices(services.toArray(AwsService[]::new))
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .waitingFor(Wait.forListeningPort()
                                .withStartupTimeout(startupTimeout));
            }

            container(localStack);
        }

        @Override
        public StartLocalStackAction doBuild() {
            return new StartLocalStackAction(this);
        }
    }
}
