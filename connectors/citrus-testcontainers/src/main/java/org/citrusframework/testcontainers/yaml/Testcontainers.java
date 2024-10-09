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

package org.citrusframework.testcontainers.yaml;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.testcontainers.actions.TestcontainersAction;

public class Testcontainers implements TestActionBuilder<TestcontainersAction>, ReferenceResolverAware {

    private AbstractTestcontainersAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private ReferenceResolver referenceResolver;

    public Testcontainers setDescription(String value) {
        this.description = description;
        return this;
    }

    public Testcontainers setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public void setStart(Start builder) {
        this.builder = builder;
    }

    public void setStop(Stop builder) {
        this.builder = builder;
    }

    @Override
    public TestcontainersAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Testcontainers action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
