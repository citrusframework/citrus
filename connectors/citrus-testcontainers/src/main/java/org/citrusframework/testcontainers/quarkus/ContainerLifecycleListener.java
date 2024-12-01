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

import java.util.Collections;
import java.util.Map;

/**
 * Listener gets invoked when Testcontainers instance is started or stopped.
 * Allows implementations to perform actions with given container instance,
 * in particular configuring the application under test with the container exposed connection settings.
 * @param <T> the container type
 */
public interface ContainerLifecycleListener<T> {

    String INIT_ARG = "citrus.testcontainers.lifecycle.listener";

    /**
     * Invoked when Testcontainers instance has been started. Returned key-value
     * map is used to set application properties on the system under test which is the Quarkus application
     * started via QuarkusTest annotation.
     * @param container
     * @return
     */
    default Map<String, String> started(T container) {
        return Collections.emptyMap();
    }

    /**
     * Invoked after the Testcontainers instance has been stopped.
     * @param container
     */
    default void stopped(T container) {
    }
}
