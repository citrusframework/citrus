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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.citrusframework.actions.testcontainers.aws2.AwsContainer;
import org.citrusframework.actions.testcontainers.aws2.AwsService;
import org.citrusframework.actions.testcontainers.aws2.ClientFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.aws2.client.DefaultClientFactoryResolver;
import org.citrusframework.util.StringUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

public class LocalStackContainer extends GenericContainer<LocalStackContainer> implements AwsContainer {

    static final int PORT = 4566;

    private static final String HOSTNAME_EXTERNAL_ENV = "HOSTNAME_EXTERNAL";
    private static final String AUTH_TOKEN_ENV = "LOCALSTACK_AUTH_TOKEN";

    private static final String DOCKER_IMAGE_NAME = LocalStackSettings.getImageName();
    private static final String DOCKER_IMAGE_TAG = LocalStackSettings.getVersion();

    private final DefaultClientFactoryResolver clientFactoryResolver = new DefaultClientFactoryResolver();
    private final Set<AwsService> services = new HashSet<>();
    private final Map<AwsService, Object> clients = new HashMap<>();

    private String authToken = LocalStackSettings.getAuthToken();

    private String secretKey = LocalStackSettings.getSecretKey();
    private String accessKey = LocalStackSettings.getAccessKey();
    private String region = LocalStackSettings.getRegion();

    public LocalStackContainer() {
        this(DOCKER_IMAGE_TAG);
    }

    public LocalStackContainer(String version, AwsService... services) {
        this(DOCKER_IMAGE_NAME, version, services);
    }

    public LocalStackContainer(String image, String version, AwsService... services) {
        super(DockerImageName.parse(image).withTag(version));

        withServices(services);
        withExposedPorts(PORT);
        waitingFor(Wait.forLogMessage(".*Ready\\.\n", 1));
    }

    public LocalStackContainer withNewServices(AwsService... services) {
        this.services.clear();
        this.services.addAll(Arrays.asList(services));
        return self();
    }

    public LocalStackContainer withServices(AwsService... services) {
        this.services.addAll(Arrays.asList(services));
        return self();
    }

    @Override
    protected void configure() {
        super.configure();

        if (services.isEmpty()) {
            throw new CitrusRuntimeException("Must provide at least one service");
        }

        if (StringUtils.hasText(authToken)) {
            // newer versions of Localstack container require auth token
            withEnv(AUTH_TOKEN_ENV, authToken);
        }

        withEnv("SERVICE", services.stream().map(AwsService::serviceName).collect(Collectors.joining(",")));

        String hostnameExternalReason;
        if (getEnvMap().containsKey(HOSTNAME_EXTERNAL_ENV)) {
            // do nothing
            hostnameExternalReason = "explicitly as environment variable";
        } else if (getNetwork() != null && !getNetworkAliases().isEmpty()) {
            withEnv(HOSTNAME_EXTERNAL_ENV, getNetworkAliases().get(getNetworkAliases().size() - 1)); // use the last network alias set
            hostnameExternalReason = "to match last network alias on container with non-default network";
        } else {
            withEnv(HOSTNAME_EXTERNAL_ENV, getHost());
            hostnameExternalReason = "to match host-routable address for container";
        }

        logger().info(
            "{} environment variable set to {} ({})",
            HOSTNAME_EXTERNAL_ENV,
            getEnvMap().get(HOSTNAME_EXTERNAL_ENV),
            hostnameExternalReason
        );
    }

    public LocalStackContainer withAuthToken(String authToken) {
        this.authToken = authToken;
        return self();
    }

    public LocalStackContainer withSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return self();
    }

    public LocalStackContainer withAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return self();
    }

    public LocalStackContainer withRegion(String region) {
        this.region = region;
        return self();
    }

    /**
     * Provides the default access key that is preconfigured on this container.
     * @return the default access key for this container.
     */
    public String getAccessKey() {
        return this.accessKey;
    }

    /**
     * Provides the secret key that is preconfigured on this container.
     * @return the default secret key for this container.
     */
    public String getSecretKey() {
        return this.secretKey;
    }

    /**
     * Provides the default region that is preconfigured on this container.
     * @return the default region for this container.
     */
    public String getRegion() {
        return this.region;
    }

    public AwsCredentialsProvider getCredentialsProvider() {
        return () -> AwsBasicCredentials.create(accessKey, secretKey);
    }

    public String getHostIpAddress() {
        try {
            return InetAddress.getByName(getHost()).getHostAddress();
        } catch (UnknownHostException e) {
            logger().warn("Unable to resolve host ip address: {}", e.getMessage());
            return getHost();
        }
    }

    public URI getServiceEndpoint() {
        try {
            return new URI("http://" + getHost() + ":" + getMappedPort(PORT));
        } catch (URISyntaxException e) {
            throw new CitrusRuntimeException(String.format("Unable to determine the service endpoint: %s", e.getMessage()), e);
        }
    }

    public AwsService[] getServices() {
        return services.toArray(AwsService[]::new);
    }

    public void addClient(AwsService service, Object client) {
        this.clients.put(service, client);
    }

    public <T> T getClient(AwsService service) {
        if (!services.contains(service)) {
            throw new CitrusRuntimeException("Unable to create client for disabled service: %s".formatted(service));
        }

        Object client = clients.get(service);
        if (client != null) {
            return (T) client;
        }

        // lazy load client for this container
        Optional<ClientFactory<?>> clientFactory = clientFactoryResolver.resolve(service);
        if (clientFactory.isPresent()) {
            client = clientFactory.get().createClient(this, Collections.emptyMap());
            clients.put(service, client);
        }

        if (client != null) {
            return (T) client;
        }

        throw new CitrusRuntimeException("Missing client for service %s".formatted(service));
    }

}
