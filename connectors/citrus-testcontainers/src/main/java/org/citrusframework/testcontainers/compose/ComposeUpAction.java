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

package org.citrusframework.testcontainers.compose;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.Base58;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class ComposeUpAction extends AbstractTestcontainersAction {

    private final ComposeContainer container;
    private final String containerName;

    private final boolean autoRemoveResources;

    private final Map<String, Integer> exposedServices;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ComposeUpAction.class);

    public ComposeUpAction(Builder builder) {
        super("compose-up", builder);

        this.container = builder.container;
        this.containerName = builder.containerName;
        this.autoRemoveResources = builder.autoRemoveResources;
        this.exposedServices = builder.exposedServices;
    }

    @Override
    public void doExecute(TestContext context) {
        container.start();

        if (containerName != null && !context.getReferenceResolver().isResolvable(containerName)) {
            context.getReferenceResolver().bind(containerName, container);
        }

        exposeConnectionSettings(container, context);

        if (autoRemoveResources) {
            context.doFinally(testcontainers()
                    .compose()
                    .down()
                    .container(container));
        }
    }

    /**
     * Sets the connection settings in current test context in the form of test variables.
     * @param container
     * @param context
     */
    protected void exposeConnectionSettings(ComposeContainer container, TestContext context) {
        for (Map.Entry<String, Integer> service : exposedServices.entrySet()) {
            String containerType = service.getKey().toUpperCase().replaceAll("-", "_").replaceAll("\\.", "_");

            Optional<ContainerState> containerState = container.getContainerByServiceName(service.getKey());
            if (containerState.isPresent()) {
                if (containerState.get().getContainerId() != null) {
                    String dockerContainerId = containerState.get().getContainerId().substring(0, 12);
                    String dockerContainerName = containerState.get().getContainerInfo().getName();

                    if (dockerContainerName.startsWith("/")) {
                        dockerContainerName = dockerContainerName.substring(1);
                    }

                    context.setVariable(getEnvVarName(containerType, "SERVICE_HOST"), container.getServiceHost(service.getKey(), service.getValue()));
                    context.setVariable(getEnvVarName(containerType, "SERVICE_PORT"), container.getServicePort(service.getKey(), service.getValue()));

                    context.setVariable(getEnvVarName(containerType, "HOST"), containerState.get().getHost());
                    context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getServiceHost(service.getKey(), service.getValue()));
                    context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
                    context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

                    if (!containerState.get().getExposedPorts().isEmpty()) {
                        context.setVariable(getEnvVarName(containerType, "PORT"), containerState.get().getFirstMappedPort());
                    }
                }
            }
        }

    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestcontainersAction.Builder<ComposeUpAction, Builder> {

        private ComposeContainer container;

        private String containerName = ComposeContainerSettings.getContainerName();

        private String filePath;

        private Resource fileResource;

        private Duration startupTimeout = Duration.ofSeconds(TestContainersSettings.getStartupTimeout());

        private boolean autoRemoveResources = TestContainersSettings.isAutoRemoveResources();

        private final Map<String, Integer> exposedServices = new HashMap<>();

        private final Map<String, WaitStrategy> waitStrategies = new HashMap<>();

        private boolean useComposeBinary = ComposeContainerSettings.isUseComposeBinary();

        public Builder() {
            withStartupTimeout(ComposeContainerSettings.getStartupTimeout());
        }

        public Builder containerName(String name) {
            this.containerName = name;
            return self;
        }

        public Builder container(ComposeContainer container) {
            this.container = container;
            return self;
        }

        public Builder container(String name, ComposeContainer container) {
            this.containerName = name;
            this.container = container;
            return self;
        }

        public Builder file(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder file(Resource fileResource) {
            this.fileResource = fileResource;
            return this;
        }

        public Builder withStartupTimeout(int timeout) {
            this.startupTimeout = Duration.ofSeconds(timeout);
            return this;
        }

        public Builder withStartupTimeout(Duration timeout) {
            this.startupTimeout = timeout;
            return this;
        }

        public Builder autoRemove(boolean enabled) {
            this.autoRemoveResources = enabled;
            return self;
        }

        public Builder useComposeBinary(boolean enabled) {
            this.useComposeBinary = enabled;
            return self;
        }

        public Builder withExposedService(String serviceName, int port) {
            this.exposedServices.put(serviceName, port);
            return self;
        }

        public Builder withExposedService(String serviceName, int port, WaitStrategy waitStrategy) {
            this.exposedServices.put(serviceName, port);
            this.waitStrategies.put(serviceName, waitStrategy);
            return self;
        }

        public Builder withExposedServices(Map<String, Integer> services) {
            exposedServices.putAll(services);
            return self;
        }

        public Builder withWaitStrategy(String serviceName, WaitStrategy waitStrategy) {
            this.waitStrategies.put(serviceName, waitStrategy);
            return self;
        }

        public Builder withWaitStrategies(Map<String, WaitStrategy> strategies) {
            waitStrategies.putAll(strategies);
            return self;
        }

        @Override
        public ComposeUpAction doBuild() {
            String identifier = StringUtils.hasText(containerName) ? containerName.toLowerCase() : Base58.randomString(6).toLowerCase();

            if (fileResource != null) {
                container = new ComposeContainer(identifier, fileResource.getFile());
            } else if (StringUtils.hasText(filePath)) {
                container = new ComposeContainer(identifier, Resources.create(filePath).getFile());
            } else if (referenceResolver != null) {
                if (StringUtils.hasText(containerName) && referenceResolver.isResolvable(containerName, ComposeContainer.class)) {
                    container = referenceResolver.resolve(containerName, ComposeContainer.class);
                } else if (referenceResolver.isResolvable(ComposeContainer.class)) {
                    container = referenceResolver.resolve(ComposeContainer.class);
                }
            }

            if (container == null && Resources.create("compose.yaml").exists()) {
                container = new ComposeContainer(identifier, Resources.create("compose.yaml").getFile());
            }

            if (container == null) {
                throw new CitrusRuntimeException("Missing proper ComposeContainer specification - either provide a container name or compose file resource");
            }

            if (useComposeBinary) {
                container.withLocalCompose(true);
            }

            for (Map.Entry<String, Integer> service : exposedServices.entrySet()) {
                WaitStrategy waitStrategy = waitStrategies.getOrDefault(service.getKey(), Wait.forListeningPort().withStartupTimeout(startupTimeout));
                container.withExposedService(service.getKey(), service.getValue(), waitStrategy);
            }

            return new ComposeUpAction(this);
        }
    }
}
