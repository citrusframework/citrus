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

package org.citrusframework.actions.testcontainers;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;

public interface TestcontainersStartActionBuilderBase<C extends AutoCloseable, T extends TestAction, B extends TestcontainersStartActionBuilderBase<C, T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T> {

    B containerName(String name);

    B serviceName(String name);

    B image(String image);

    B container(C container);

    @SuppressWarnings("unchecked")
    default B container(Object container) {
        try {
            return container((C) container);
        } catch (ClassCastException e) {
            throw new CitrusRuntimeException("Invalid container type '%s' for start container action type '%s'"
                    .formatted(container.getClass().getName() ,getClass().getName()));
        }
    }

    B container(String name, C container);

    @SuppressWarnings("unchecked")
    default B container(String name, Object container) {
        try {
            return container(name, (C) container);
        } catch (ClassCastException e) {
            throw new CitrusRuntimeException("Invalid container type '%s' for start container action type '%s'"
                    .formatted(container.getClass().getName() ,getClass().getName()));
        }
    }

    B withStartupTimeout(int timeout);

    B withStartupTimeout(Duration timeout);

    B withNetwork();

    B withNetwork(Object network);

    B withoutNetwork();

    B withEnv(String key, String value);

    B withEnv(Map<String, String> env);

    B withLabel(String label, String value);

    B withLabels(Map<String, String> labels);

    B withCommand(String... command);

    B autoRemove(boolean enabled);

    B addExposedPort(int port);

    B addExposedPorts(int... ports);

    B addExposedPorts(List<Integer> ports);

    B addPortBinding(String binding);

    B addPortBindings(String... bindings);

    B addPortBindings(List<String> bindings);

    B waitFor(Object o);

    B waitFor(URL url);

    B waitFor(String logMessage);

    B waitFor(String logMessage, int times);

    B waitStrategyDisabled();

    B withVolumeMount(Object o, String containerPath);

    B withVolumeMount(String mountableFile, String mountPath);

    B withVolumeMount(Resource mountableFile, String mountPath);
}
