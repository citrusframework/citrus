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

import java.util.Optional;

import org.citrusframework.actions.testcontainers.TestcontainersStopActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testcontainers.containers.GenericContainer;

public class StopTestcontainersAction extends AbstractTestcontainersAction {

    private final String containerName;
    private final GenericContainer<?> container;

    public StopTestcontainersAction(Builder builder) {
        super("stop", builder);

        this.containerName = builder.containerName;
        this.container = builder.container;
    }

    @Override
    public void doExecute(TestContext context) {
        if (container != null) {
            stop(container, "");
        } else if (containerName != null) {
            if (context.getReferenceResolver().isResolvable(containerName)) {
                Object maybeContainer = context.getReferenceResolver().resolve(containerName);
                if (maybeContainer instanceof GenericContainer<?> genericContainer) {
                    stop(genericContainer, containerName);
                }
            } else {
                logger.warn("Unable to stop Testcontainers container '{}'", containerName);
            }
        } else {
            throw new CitrusRuntimeException("Unable to stop Testcontainers container - neither container nor container name provided");
        }
    }

    private void stop(GenericContainer<?> container, String name) {
        String containerName = getContainerName(container, name);
        if (container.isRunning()) {
            String containerId = Optional.ofNullable(container.getContainerId()).orElse("unknown");
            container.stop();
            logger.info("Successfully stopped Testcontainers container '{}' with id {}", containerName, containerId);
        } else {
            logger.info("Testcontainers container '{}' is stopped", containerName);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestcontainersAction.Builder<StopTestcontainersAction, Builder>
            implements TestcontainersStopActionBuilder<StopTestcontainersAction, Builder> {

        protected String containerName;
        private GenericContainer<?> container;

        @Override
        public Builder containerName(String name) {
            this.containerName = name;
            return this;
        }

        @Override
        public Builder container(Object o) {
            if (o instanceof GenericContainer<?> genericContainer) {
                this.container = genericContainer;
            } else {
                throw new CitrusRuntimeException("");
            }

            return this;
        }

        public Builder container(GenericContainer<?> container) {
            this.container = container;
            return this;
        }

        @Override
        public StopTestcontainersAction doBuild() {
            return new StopTestcontainersAction(this);
        }
    }
}
