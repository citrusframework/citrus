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

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.floci.testcontainers.FlociContainer;
import org.citrusframework.actions.testcontainers.TestcontainersFlociStartActionBuilder;
import org.citrusframework.actions.testcontainers.aws2.AwsContainer;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.actions.testcontainers.aws2.ClientFactory;
import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.TestContainersSettings;
import org.citrusframework.testcontainers.actions.StartTestcontainersAction;
import org.citrusframework.testcontainers.aws2.client.DefaultClientFactoryResolver;
import org.citrusframework.util.PropertyUtils;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class StartFlociAction extends StartTestcontainersAction<FlociContainer> {

    private static final Logger logger = LoggerFactory.getLogger(StartFlociAction.class);

    private final DefaultClientFactoryResolver clientFactoryResolver = new DefaultClientFactoryResolver();
    private final boolean autoCreateClients;

    private final Set<AwsService> services;
    private final Map<String, String> options;
    private final String region;

    public StartFlociAction(Builder builder) {
        super(builder);
        this.autoCreateClients = builder.autoCreateClients;
        this.services = builder.services;
        this.options = builder.options;
        this.region = builder.region;
    }

    @Override
    protected void configure(FlociContainer container, TestContext context) {
        if (StringUtils.hasText(region)) {
            container.withRegion(context.replaceDynamicContentInString(region));
        }
    }

    @Override
    protected void exposeConnectionSettings(FlociContainer container, TestContext context) {
        FlociSettings.exposeConnectionSettings(container, services, serviceName, context);

        if (autoCreateClients) {
            for (AwsService service : services) {
                String clientName = "%sClient".formatted(service.getServiceName());
                clientName = options.getOrDefault(clientName + "Name", clientName);

                if (context.getReferenceResolver().isResolvable(clientName)) {
                    logger.debug("Client {} already exists - do not overwrite", clientName);
                    // client bean with same name already exists - do not overwrite
                    continue;
                }

                Optional<ClientFactory<?>> clientFactory = clientFactoryResolver.resolve(context.getReferenceResolver(), service);
                if (clientFactory.isPresent()) {
                    Object client = clientFactory.get().createClient(new AwsContainerWrapper(container), context.resolveDynamicValuesInMap(options));
                    PropertyUtils.configure(clientName, client, context.getReferenceResolver());
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
    public static class Builder extends AbstractBuilder<FlociContainer, StartFlociAction, Builder>
            implements TestcontainersFlociStartActionBuilder<FlociContainer, StartFlociAction, Builder> {

        private String flociVersion = FlociSettings.getVersion();

        private final Set<AwsService> services = new HashSet<>();

        private boolean autoCreateClients = FlociSettings.isAutoCreateClients();

        private final Map<String, String> options = new HashMap<>();

        private String accountId = FlociSettings.getAccountId();
        private String availabilityZone = FlociSettings.getAvailabilityZone();
        private String region = FlociSettings.getRegion();

        public Builder() {
            withStartupTimeout(FlociSettings.getStartupTimeout());
        }

        @Override
        public Builder version(String flociVersion) {
           this.flociVersion = flociVersion;
           return this;
        }

        @Override
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }

        @Override
        public Builder availabilityZone(String availabilityZone) {
            this.availabilityZone = availabilityZone;
            return this;
        }

        @Override
        public Builder region(String region) {
            this.region = region;
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
                containerName(FlociSettings.getContainerName());
            }

            if (serviceName == null) {
                serviceName(FlociSettings.getServiceName());
            }

            if (image == null) {
                image(FlociSettings.getImageName());
            }

            withLabel("app", "citrus");
            withLabel("com.joyrex2001.kubedock.name-prefix", serviceName);
            withLabel("app.kubernetes.io/name", "build");
            withLabel("app.kubernetes.io/part-of", TestContainersSettings.getTestName());
            withLabel("app.openshift.io/connects-to", TestContainersSettings.getTestId());

            FlociContainer floci;
            if (referenceResolver != null && referenceResolver.isResolvable(containerName, FlociContainer.class)) {
                floci = referenceResolver.resolve(containerName, FlociContainer.class);
            } else {
                floci = new FlociContainer(DockerImageName.parse(image).withTag(flociVersion))
                        .withNetwork(network)
                        .withNetworkAliases(serviceName)
                        .withDefaultAccountId(accountId)
                        .withDefaultAvailabilityZone(availabilityZone)
                        .waitingFor(Wait.forHttp("/_floci/health")
                                .forPort(FlociContainer.PORT)
                                .withStartupTimeout(startupTimeout));
            }

            container(floci);
        }

        @Override
        public StartFlociAction doBuild() {
            return new StartFlociAction(this);
        }
    }

    /**
     * Wrapper for Floc container to implement common AWS container interface.
     */
    public record AwsContainerWrapper(FlociContainer delegate) implements AwsContainer {

        @Override
        public URI getServiceEndpoint() {
            return URI.create(delegate.getEndpoint());
        }

        @Override
        public String getRegion() {
            return delegate.getRegion();
        }

        @Override
        public String getAccessKey() {
            return delegate.getAccessKey();
        }

        @Override
        public String getSecretKey() {
            return delegate.getSecretKey();
        }
    }
}
