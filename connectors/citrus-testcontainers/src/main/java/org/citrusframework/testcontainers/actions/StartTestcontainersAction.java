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

package org.citrusframework.testcontainers.actions;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citrusframework.context.TestContext;
import org.citrusframework.spi.Resource;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.WaitStrategyHelper;
import org.citrusframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.utility.MountableFile;

import static org.citrusframework.testcontainers.TestcontainersHelper.getEnvVarName;
import static org.citrusframework.testcontainers.actions.TestcontainersActionBuilder.testcontainers;

public class StartTestcontainersAction<C extends GenericContainer<?>> extends AbstractTestcontainersAction {

    protected final String serviceName;
    protected final String containerName;
    private final C container;
    private final boolean autoRemoveResources;

    public StartTestcontainersAction(AbstractBuilder<C, ? extends StartTestcontainersAction<C>, ?> builder) {
        super("start", builder);

        this.serviceName = builder.serviceName;
        this.containerName = builder.containerName;
        this.container = builder.container;
        this.autoRemoveResources = builder.autoRemoveResources;
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
                    .stop()
                    .container(container));
        }
    }

    /**
     * Sets the connection settings in current test context in the form of test variables.
     * @param container
     * @param context
     */
    protected void exposeConnectionSettings(C container, TestContext context) {
        if (container.getContainerId() != null) {
            String dockerContainerId = container.getContainerId().substring(0, 12);
            String dockerContainerName = container.getContainerName();

            if (dockerContainerName.startsWith("/")) {
                dockerContainerName = dockerContainerName.substring(1);
            }

            if (containerName != null) {
                String containerType = containerName.toUpperCase().replaceAll("-", "_").replaceAll("\\.", "_");
                context.setVariable(getEnvVarName(containerType, "HOST"), container.getHost());
                context.setVariable(getEnvVarName(containerType, "CONTAINER_IP"), container.getHost());
                context.setVariable(getEnvVarName(containerType, "CONTAINER_ID"), dockerContainerId);
                context.setVariable(getEnvVarName(containerType, "CONTAINER_NAME"), dockerContainerName);

                if (!container.getExposedPorts().isEmpty()) {
                    context.setVariable(getEnvVarName(containerType, "PORT"), container.getFirstMappedPort());
                }
            }
        }
    }

    public C getContainer() {
        return container;
    }

    public static class Builder<C extends GenericContainer<?>> extends AbstractBuilder<C, StartTestcontainersAction<C>, Builder<C>> {
        @Override
        protected StartTestcontainersAction<C> doBuild() {
            return new StartTestcontainersAction<>(this);
        }
    }

    /**
     * Abstract start action builder.
     */
    public static abstract class AbstractBuilder<C extends GenericContainer<?>, T extends StartTestcontainersAction<C>, B extends AbstractBuilder<C, T, B>> extends AbstractTestcontainersAction.Builder<T, B> {

        protected String image;
        protected String containerName;
        protected String serviceName;
        private final Map<String, String> labels = new HashMap<>();
        protected final Map<String, String> env = new HashMap<>();
        private final List<String> commandLine = new ArrayList<>();
        protected C container;
        protected Network network;
        protected Duration startupTimeout = Duration.ofSeconds(TestContainersSettings.getStartupTimeout());

        protected final Set<Integer> exposedPorts = new HashSet<>();
        protected final List<String> portBindings = new ArrayList<>();

        protected final Map<MountableFile, String> volumeMounts = new HashMap<>();

        protected WaitStrategy waitStrategy;

        private boolean autoRemoveResources = TestContainersSettings.isAutoRemoveResources();

        public B containerName(String name) {
            this.containerName = name;
            return self;
        }

        public B serviceName(String name) {
            this.serviceName = name;
            return self;
        }

        public B image(String image) {
            this.image = image;
            return self;
        }

        public B container(C container) {
            this.container = container;
            return self;
        }

        public B container(String name, C container) {
            this.containerName = name;
            this.container = container;
            return self;
        }

        public B withStartupTimeout(int timeout) {
            this.startupTimeout = Duration.ofSeconds(timeout);
            return self;
        }

        public B withStartupTimeout(Duration timeout) {
            this.startupTimeout = timeout;
            return self;
        }

        public B withNetwork() {
            network = Network.newNetwork();
            return self;
        }

        public B withNetwork(Network network) {
            this.network = network;
            return self;
        }

        public B withoutNetwork() {
            network = null;
            return self;
        }

        public B withEnv(String key, String value) {
            this.env.put(key, value);
            return self;
        }

        public B withEnv(Map<String, String> env) {
            this.env.putAll(env);
            return self;
        }

        public B withLabel(String label, String value) {
            this.labels.put(label, value);
            return self;
        }

        public B withLabels(Map<String, String> labels) {
            this.labels.putAll(labels);
            return self;
        }

        public B withCommand(String... command) {
            this.commandLine.addAll(List.of(command));
            return self;
        }

        public B autoRemove(boolean enabled) {
            this.autoRemoveResources = enabled;
            return self;
        }

        public B addExposedPort(int port) {
            this.exposedPorts.add(port);
            return self;
        }

        public B addExposedPorts(int... ports) {
            for (int port : ports) {
                addExposedPort(port);
            }
            return self;
        }

        public B addExposedPorts(List<Integer> ports) {
            exposedPorts.addAll(ports);
            return self;
        }

        public B addPortBinding(String binding) {
            this.portBindings.add(binding);
            return self;
        }

        public B addPortBindings(String... bindings) {
            for (String binding : bindings) {
                addPortBinding(binding);
            }
            return self;
        }

        public B addPortBindings(List<String> bindings) {
            portBindings.addAll(bindings);
            return self;
        }

        public B waitFor(WaitStrategy waitStrategy) {
            this.waitStrategy = waitStrategy;
            return self;
        }

        public B waitFor(URL url) {
            this.waitStrategy = WaitStrategyHelper.waitFor(url);
            return self;
        }

        public B waitFor(String logMessage) {
            return waitFor(logMessage, 1);
        }

        public B waitFor(String logMessage, int times) {
            this.waitStrategy = Wait.forLogMessage(logMessage, times);
            return self;
        }

        public B waitStrategyDisabled() {
            this.waitStrategy = WaitStrategyHelper.getNoopStrategy();
            return self;
        }

        public B withVolumeMount(MountableFile mountableFile, String containerPath) {
            this.volumeMounts.put(mountableFile, containerPath);
            return self;
        }

        public B withVolumeMount(String mountableFile, String mountPath) {
            return withVolumeMount(Resources.create(mountableFile), mountPath);
        }

        public B withVolumeMount(Resource mountableFile, String mountPath) {
            if (mountableFile instanceof Resources.ClasspathResource) {
                this.volumeMounts.put(MountableFile.forClasspathResource(mountableFile.getLocation()), mountPath);
            } else if (mountableFile instanceof Resources.FileSystemResource) {
                this.volumeMounts.put(MountableFile.forHostPath(mountableFile.getFile().getAbsolutePath()), mountPath);
            } else {
                this.volumeMounts.put(MountableFile.forHostPath(mountableFile.getLocation()), mountPath);
            }
            return self;
        }

        protected void prepareBuild() {
        }

        protected C buildContainer() {
            String imageName;
            if (StringUtils.hasText(TestContainersSettings.getRegistry()) &&
                    !image.startsWith(TestContainersSettings.getDockerRegistry())) {
                imageName = TestContainersSettings.getDockerRegistry() + image;
            } else {
                imageName = image;
            }

            C container = (C) new GenericContainer<>(imageName);

            if (network != null) {
                container.withNetwork(network);
                if (serviceName != null) {
                    container.withNetworkAliases(serviceName);
                } else if (containerName != null) {
                    container.withNetworkAliases(containerName);
                }
            }

            return container;
        }

        protected void configureContainer(C container) {
            container.withLabels(labels);
            container.withEnv(env);

            exposedPorts.forEach(container::addExposedPort);
            container.setPortBindings(portBindings);

            volumeMounts.forEach(container::withCopyFileToContainer);

            if (waitStrategy != null) {
                container.waitingFor(waitStrategy);
            }

            container.withStartupTimeout(startupTimeout);

            if (!commandLine.isEmpty()) {
                container.withCommand(commandLine.toArray(String[]::new));
            }
        }

        @Override
        public T build() {
            prepareBuild();

            if (container == null) {
                container = buildContainer();
            }

            configureContainer(container);

            if (containerName == null && image != null) {
                if (image.contains(":")) {
                    containerName = image.split(":")[0];
                } else {
                    containerName = image;
                }
            }

            return doBuild();
        }
    }
}
