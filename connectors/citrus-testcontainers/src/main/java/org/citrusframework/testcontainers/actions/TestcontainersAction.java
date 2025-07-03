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

package org.citrusframework.testcontainers.actions;

import org.citrusframework.TestAction;
import org.testcontainers.containers.GenericContainer;

/**
 * Base action representing interactions with Testcontainers.
 */
public interface TestcontainersAction extends TestAction {

    default String getContainerName(GenericContainer<?> container, String containerName) {
        String name = container.getDockerImageName();

        if (container.getContainerInfo() != null) {
            name = container.getContainerInfo().getName();
        } else if (container.getContainerId() != null) {
            name = container.getCurrentContainerInfo().getName();
        }

        name = name.startsWith("/") ? name.substring(1) : name;

        if (containerName != null && !containerName.equals(name)) {
            return "%s (%s)".formatted(containerName, name);
        }

        return name;
    }

}

