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

package org.citrusframework.testcontainers.aws2.quarkus;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.aws2.LocalStackContainer;
import org.citrusframework.testcontainers.aws2.StartLocalStackAction;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.citrusframework.testcontainers.quarkus.TestcontainersResource;

public class LocalStackContainerResource extends TestcontainersResource<LocalStackContainer>
        implements QuarkusTestResourceConfigurableLifecycleManager<LocalStackContainerSupport> {

    public static final String SERVICES_INIT_ARG = "aws.localstack.services";

    public LocalStackContainerResource() {
        super(LocalStackContainer.class);
    }

    @Override
    public void init(LocalStackContainerSupport config) {
        for (Class<? extends ContainerLifecycleListener<LocalStackContainer>> lifecycleListenerType :
                config.containerLifecycleListener()) {
            try {
                registerContainerLifecycleListener(lifecycleListenerType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s"
                        .formatted(lifecycleListenerType), e);
            }
        }

        container = new StartLocalStackAction.Builder()
                .withServices(config.services())
                .build().getContainer();
    }

    @Override
    protected void doInit(Map<String, String> initArgs) {
        String[] serviceNames = initArgs.getOrDefault(SERVICES_INIT_ARG, "").split(",");
        container = new StartLocalStackAction.Builder()
                .withServices(Arrays.stream(serviceNames)
                        .map(String::trim)
                        .map(LocalStackContainer.Service::fromServiceName)
                        .toArray(LocalStackContainer.Service[]::new))
                .build().getContainer();
    }
}
