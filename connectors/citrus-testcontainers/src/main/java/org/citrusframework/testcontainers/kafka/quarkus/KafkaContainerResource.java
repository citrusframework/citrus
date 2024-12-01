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

package org.citrusframework.testcontainers.kafka.quarkus;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.citrusframework.testcontainers.quarkus.TestcontainersResource;
import org.testcontainers.containers.KafkaContainer;

public class KafkaContainerResource extends TestcontainersResource<KafkaContainer>
        implements QuarkusTestResourceConfigurableLifecycleManager<KafkaContainerSupport> {

    public KafkaContainerResource() {
        super(KafkaContainer.class);
    }

    @Override
    public void init(KafkaContainerSupport config) {
        for (Class<? extends ContainerLifecycleListener<KafkaContainer>> lifecycleListenerType :
                config.containerLifecycleListener()) {
            try {
                registerContainerLifecycleListener(lifecycleListenerType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s"
                        .formatted(lifecycleListenerType), e);
            }
        }

        doInit(Collections.emptyMap());
    }

    @Override
    protected void doInit(Map<String, String> initArgs) {
        container = new StartKafkaAction.Builder().build().getContainer();
    }
}
