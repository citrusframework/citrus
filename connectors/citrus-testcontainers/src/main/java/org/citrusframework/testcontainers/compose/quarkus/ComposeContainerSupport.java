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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.common.QuarkusTestResource;
import org.citrusframework.testcontainers.quarkus.ContainerLifecycleListener;
import org.testcontainers.containers.ComposeContainer;

@QuarkusTestResource(ComposeContainerResource.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComposeContainerSupport {

    /**
     * Path to the Docker Compose file. When empty, defaults to "compose.yaml" on the classpath.
     */
    String composeFile() default "";

    /**
     * Services to expose from the compose environment.
     */
    ExposedService[] exposedServices() default {};

    /**
     * Startup timeout in seconds. When set to -1, uses the default from ComposeContainerSettings.
     */
    int startupTimeout() default -1;

    /**
     * Container lifecycle listeners.
     */
    Class<? extends ContainerLifecycleListener<ComposeContainer>>[] containerLifecycleListener() default {};

    @Retention(RetentionPolicy.RUNTIME)
    @Target({})
    @interface ExposedService {

        /**
         * The service name as defined in the compose file.
         */
        String name();

        /**
         * The port exposed by the service.
         */
        int port();
    }
}
