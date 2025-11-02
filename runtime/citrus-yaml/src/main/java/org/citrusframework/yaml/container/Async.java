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

package org.citrusframework.yaml.container;

import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.TestActions;

public class Async implements TestActionBuilder<org.citrusframework.container.Async>, ReferenceResolverAware {

    private final org.citrusframework.container.Async.Builder builder = new org.citrusframework.container.Async.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public org.citrusframework.container.Async build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    @SchemaProperty(required = true, description = "Sequence of test actions to execute.")
    public void setActions(List<TestActions> actions) {
        builder.actions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @SchemaProperty(advanced = true, description = "Test actions executed when async container is successful.")
    public void setSuccess(List<TestActions> actions) {
        builder.successActions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @SchemaProperty(advanced = true, description = "Test actions executed when async container has failed.")
    public void setError(List<TestActions> actions) {
        builder.errorActions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
