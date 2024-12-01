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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testcontainers.containers.GenericContainer;

public class TestcontainersResource<T extends GenericContainer<?>> implements QuarkusTestResourceLifecycleManager {

    private final Set<ContainerLifecycleListener<T>> containerLifecycleListeners = new HashSet<>();

    protected T container;

    private final Class<T> containerType;

    public TestcontainersResource(Class<T> containerType) {
        this.containerType = containerType;
    }

    @Override
    public final void init(Map<String, String> initArgs) {
        String[] qualifiedClassNames = initArgs.getOrDefault(ContainerLifecycleListener.INIT_ARG, "").split(",");
        for (String qualifiedClassName : qualifiedClassNames) {
            try {
                Class<?> cls = Class.forName(qualifiedClassName, true, Thread.currentThread().getContextClassLoader());
                Object instance = cls.getDeclaredConstructor().newInstance();
                if (instance instanceof ContainerLifecycleListener<?> containerLifecycleListener) {
                    registerContainerLifecycleListener((ContainerLifecycleListener<T>) containerLifecycleListener);
                }
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new CitrusRuntimeException("Failed to instantiate container lifecycle listener from type: %s".formatted(qualifiedClassName), e);
            }
        }

        doInit(initArgs);
    }

    protected void doInit(Map<String, String> initArgs) {
    }

    @Override
    public final Map<String, String> start() {
        Map<String, String> conf = doStart();
        for (ContainerLifecycleListener<T> listener : containerLifecycleListeners) {
            conf.putAll(listener.started(container));
        }
        return conf;
    }

    protected Map<String, String> doStart() {
        container.start();
        return new HashMap<>();
    }

    @Override
    public final void stop() {
        doStop();
        containerLifecycleListeners.forEach(listener -> listener.stopped(container));
    }

    protected void doStop() {
        container.stop();
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(container, new TestInjector.AnnotatedAndMatchesType(CitrusResource.class, containerType));
    }

    protected void registerContainerLifecycleListener(ContainerLifecycleListener<T> containerLifecycleListener) {
        this.containerLifecycleListeners.add(containerLifecycleListener);
    }

}
