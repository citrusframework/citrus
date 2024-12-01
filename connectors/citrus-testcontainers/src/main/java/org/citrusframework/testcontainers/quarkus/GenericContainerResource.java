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

package org.citrusframework.testcontainers.quarkus;

import java.lang.reflect.InvocationTargetException;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testcontainers.containers.GenericContainer;

public class GenericContainerResource extends TestcontainersResource<GenericContainer<?>>
        implements QuarkusTestResourceConfigurableLifecycleManager<TestcontainersSupport> {

    public GenericContainerResource() {
        super((Class) GenericContainer.class);
    }

    @Override
    public void init(TestcontainersSupport config) {
        for (Class<? extends ContainerLifecycleListener<? extends GenericContainer<?>>> lifecycleListenerType :
                config.containerLifecycleListener()) {
            try {
                registerContainerLifecycleListener((ContainerLifecycleListener<GenericContainer<?>>) lifecycleListenerType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s"
                        .formatted(lifecycleListenerType), e);
            }
        }

        try {
            container = config.containerProvider().getDeclaredConstructor().newInstance().create();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new CitrusRuntimeException("Failed to instantiate container provider from type: %s"
                    .formatted(config.containerProvider()), e);
        }
    }
}
