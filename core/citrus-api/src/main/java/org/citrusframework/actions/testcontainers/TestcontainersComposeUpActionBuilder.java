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

import java.time.Duration;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.spi.Resource;

public interface TestcontainersComposeUpActionBuilder<C extends AutoCloseable, T extends TestAction, B extends TestcontainersComposeUpActionBuilder<C, T, B>>
        extends ActionBuilder<T, B>, TestActionBuilder<T> {

    B containerName(String name);

    B container(C container);

    B container(String name, C container);

    B file(String filePath);

    B file(Resource fileResource);

    B withStartupTimeout(int timeout);

    B withStartupTimeout(Duration timeout);

    B autoRemove(boolean enabled);

    B useComposeBinary(boolean enabled);

    B withExposedService(String serviceName, int port);

    B withExposedService(String serviceName, int port, Object waitStrategy);

    B withExposedServices(Map<String, Integer> services);

    B withWaitStrategy(String serviceName, Object waitStrategy);

    B withWaitStrategies(Map<String, Object> strategies);
}
