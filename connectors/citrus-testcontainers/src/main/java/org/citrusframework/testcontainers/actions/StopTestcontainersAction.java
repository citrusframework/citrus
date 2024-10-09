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

import org.citrusframework.context.TestContext;
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
            container.stop();
        } else if (containerName != null && context.getReferenceResolver().isResolvable(containerName)) {
            Object maybeContainer = context.getReferenceResolver().resolve(containerName);
            if (maybeContainer instanceof GenericContainer<?> genericContainer) {
                genericContainer.stop();
            }
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestcontainersAction.Builder<StopTestcontainersAction, Builder> {

        protected String containerName;
        private GenericContainer<?> container;

        public Builder containerName(String name) {
            this.containerName = name;
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
