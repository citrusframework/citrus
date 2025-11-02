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

import org.citrusframework.TestActor;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.testcontainers.actions.AbstractTestcontainersAction;
import org.citrusframework.testcontainers.actions.StopTestcontainersAction;
import org.citrusframework.yaml.SchemaProperty;

public class Stop extends AbstractTestcontainersAction.Builder<StopTestcontainersAction, Stop> implements ReferenceResolverAware {

    private final StopTestcontainersAction.Builder delegate = new StopTestcontainersAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        delegate.containerName(name);
    }

    @Override
    public Stop description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Stop actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public StopTestcontainersAction doBuild() {
        return delegate.build();
    }
}
