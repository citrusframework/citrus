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

package org.citrusframework.testcontainers.kafka.quarkus.strimzi;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.testcontainers.kafka.KafkaImplementation;
import org.citrusframework.testcontainers.kafka.StartKafkaAction;
import org.citrusframework.testcontainers.kafka.StrimziContainer;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.citrusframework.testcontainers.quarkus.TestcontainersResource;

public class KafkaContainerResource extends TestcontainersResource<StrimziContainer>
        implements QuarkusTestResourceConfigurableLifecycleManager<KafkaContainerSupport> {

    public KafkaContainerResource() {
        super(StrimziContainer.class);
    }

    @Override
    public void init(KafkaContainerSupport config) {
        for (Class<? extends ContainerLifecycleListener<StrimziContainer>> lifecycleListenerType :
                config.containerLifecycleListener()) {
            try {
                registerContainerLifecycleListener(lifecycleListenerType.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s"
                        .formatted(lifecycleListenerType), e);
            }
        }

        Map<String, String> initArgs = new HashMap<>();
        if (!config.version().isEmpty()) {
            initArgs.put("version", config.version());
        }

        if (config.port() > 0) {
            initArgs.put("port",  String.valueOf(config.port()));
        }

        if (config.topics().length > 0) {
            initArgs.put("topics", String.join(",", config.topics()));
        }

        doInit(initArgs);
    }

    @Override
    protected void doInit(Map<String, String> initArgs) {
        StartKafkaAction.Builder<StrimziContainer> builder = new StartKafkaAction.Builder<StrimziContainer>()
                .implementation(KafkaImplementation.STRIMZI);

        if (initArgs.containsKey("version")) {
            builder.version(initArgs.get("version"));
        }

        if (initArgs.containsKey("port")) {
            builder.port(Integer.parseInt(initArgs.get("port")));
        }

        if (initArgs.containsKey("topics")) {
            builder.topics(initArgs.get("topics").split(","));
        }

        container = builder.build().getContainer();
    }
}
