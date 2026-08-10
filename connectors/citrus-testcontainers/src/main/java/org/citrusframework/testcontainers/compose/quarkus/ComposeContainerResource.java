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

package org.citrusframework.testcontainers.compose.quarkus;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resources;
import org.citrusframework.testcontainers.compose.ComposeContainerSettings;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.citrusframework.util.StringUtils;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.Base58;

public class ComposeContainerResource
        implements QuarkusTestResourceLifecycleManager,
        QuarkusTestResourceConfigurableLifecycleManager<ComposeContainerSupport> {

    private final Set<ContainerLifecycleListener<ComposeContainer>> containerLifecycleListeners = new HashSet<>();

    protected ComposeContainer container;

    @Override
    public void init(ComposeContainerSupport config) {
        for (Class<? extends ContainerLifecycleListener<ComposeContainer>> lifecycleListenerType :
                config.containerLifecycleListener()) {
            try {
                containerLifecycleListeners.add(lifecycleListenerType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s"
                        .formatted(lifecycleListenerType), e);
            }
        }

        String composeFile = config.composeFile();
        int startupTimeout = config.startupTimeout();
        ComposeContainerSupport.ExposedService[] exposedServices = config.exposedServices();

        String identifier = Base58.randomString(6).toLowerCase();

        if (StringUtils.hasText(composeFile)) {
            container = new ComposeContainer(identifier, Resources.create(composeFile).file());
        } else if (Resources.create("compose.yaml").exists()) {
            container = new ComposeContainer(identifier, Resources.create("compose.yaml").file());
        }

        if (container == null) {
            throw new CitrusRuntimeException(
                    "Missing proper ComposeContainer specification - " +
                            "provide a compose file via the composeFile attribute or place a compose.yaml on the classpath");
        }

        Duration timeout = startupTimeout > 0
                ? Duration.ofSeconds(startupTimeout)
                : Duration.ofSeconds(ComposeContainerSettings.getStartupTimeout());

        for (ComposeContainerSupport.ExposedService service : exposedServices) {
            container.withExposedService(service.name(), service.port(),
                    Wait.forListeningPort().withStartupTimeout(timeout));
        }
    }

    @Override
    public Map<String, String> start() {
        container.start();

        Map<String, String> conf = new HashMap<>();
        for (ContainerLifecycleListener<ComposeContainer> listener : containerLifecycleListeners) {
            conf.putAll(listener.started(container));
        }
        return conf;
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
        containerLifecycleListeners.forEach(listener -> listener.stopped(container));
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(container,
                new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, ComposeContainer.class));
    }
}
