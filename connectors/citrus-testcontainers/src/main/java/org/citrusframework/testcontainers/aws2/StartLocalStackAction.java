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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.Wait;

public class StartLocalStackAction extends StartTestcontainersAction<LocalStackContainer> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StartLocalStackAction.class);

    private final boolean autoCreateClients;

    public StartLocalStackAction(Builder builder) {
        super(builder);
        this.autoCreateClients = builder.autoCreateClients;
    }

    @Override
    protected void exposeConnectionSettings(LocalStackContainer container, TestContext context) {
        LocalStackSettings.exposeConnectionSettings(container, serviceName, context);

        if (autoCreateClients) {
            for (LocalStackContainer.Service service : container.getServices()) {
                String clientName = "%sClient".formatted(service.getServiceName());
                if (context.getReferenceResolver().isResolvable(clientName)) {
                    // client bean with same name already exists - do not overwrite
                    continue;
                }

                Optional<ClientFactory<?>> clientFactory = ClientFactory.lookup(context.getReferenceResolver(), service);
                if (clientFactory.isPresent()) {
                    Object client = clientFactory.get().createClient(container);
                    container.addClient(service, client);
                    context.getReferenceResolver().bind(clientName, client);
                } else {
                    logger.warn("Missing client factory for service '%s' - no client created for this service".formatted(service));
                }
            }
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractBuilder<LocalStackContainer, StartLocalStackAction, Builder> {

        private String localStackVersion = LocalStackSettings.getVersion();

        private final Set<LocalStackContainer.Service> services = new HashSet<>();

        private boolean autoCreateClients = LocalStackSettings.isAutoCreateClients();

        public Builder() {
            withStartupTimeout(LocalStackSettings.getStartupTimeout());
        }

        public Builder version(String localStackVersion) {
           this.localStackVersion = localStackVersion;
           return this;
        }

        public Builder withService(LocalStackContainer.Service service) {
           this.services.add(service);
           return this;
        }

        public Builder withServices(LocalStackContainer.Service[] services) {
            this.services.addAll(Arrays.asList(services));
           return this;
        }

        public Builder withServices(Set<LocalStackContainer.Service> services) {
            this.services.addAll(services);
           return this;
        }

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
            } else {
                localStack = new LocalStackContainer(image, localStackVersion)
                        .withServices(services.toArray(LocalStackContainer.Service[]::new))
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
