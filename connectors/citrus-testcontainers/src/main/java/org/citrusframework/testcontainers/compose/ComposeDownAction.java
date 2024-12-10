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

package org.citrusframework.testcontainers.compose;

import org.citrusframework.context.TestContext;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.testcontainers.containers.ComposeContainer;

public class ComposeDownAction extends AbstractTestcontainersAction {

    private final String containerName;
    private final ComposeContainer container;

    public ComposeDownAction(Builder builder) {
        super("compose-down", builder);

        this.containerName = builder.containerName;
        this.container = builder.container;
    }

    @Override
    public void doExecute(TestContext context) {
        if (container != null) {
            container.stop();
        } else if (containerName != null && context.getReferenceResolver().isResolvable(containerName)) {
            Object maybeContainer = context.getReferenceResolver().resolve(containerName);
            if (maybeContainer instanceof ComposeContainer composeContainer) {
                composeContainer.stop();
            }
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestcontainersAction.Builder<ComposeDownAction, Builder> {

        protected String containerName;
        private ComposeContainer container;

        public Builder containerName(String name) {
            this.containerName = name;
            return this;
        }

        public Builder container(ComposeContainer container) {
            this.container = container;
            return this;
        }

        @Override
        public ComposeDownAction doBuild() {
            return new ComposeDownAction(this);
        }
    }
}
