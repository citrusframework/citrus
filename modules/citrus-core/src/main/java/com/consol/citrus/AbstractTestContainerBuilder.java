/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.consol.citrus.actions.NoopTestAction;
import com.consol.citrus.container.TestActionContainer;

/**
 * Abstract container builder takes care on calling the container runner when actions are placed in the container.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestContainerBuilder<T extends TestActionContainer, S extends AbstractTestContainerBuilder<T, S>> extends AbstractTestActionBuilder<T, S> implements TestActionContainerBuilder<T, S> {

    protected final List<TestActionBuilder<?>> actions = new ArrayList<>();

    @Override
    public S actions(TestAction... actions) {
        return actions(Stream.of(actions)
                                .filter(action -> !(action instanceof NoopTestAction))
                                .map(action -> (TestActionBuilder<?>)() -> action)
                                .collect(Collectors.toList())
                                .toArray(new TestActionBuilder<?>[]{}));
    }

    @Override
    public S actions(TestActionBuilder<?>... actions) {
        for (int i = 0; i < actions.length; i++) {
            TestActionBuilder<?> current = actions[i];

            if (current.build() instanceof NoopTestAction) {
                continue;
            }

            if (this.actions.size() == i) {
                this.actions.add(current);
            } else if (!resolveActionBuilder(this.actions.get(i)).equals(resolveActionBuilder(current))) {
                this.actions.add(i, current);
            }
        }
        return self;
    }

    /**
     * Resolve action builder and takes care of delegating builders.
     * @param builder the builder maybe a delegating builder.
     * @return the builder itself or the delegate builder if this builder is a delegating builder.
     */
    private TestActionBuilder<?> resolveActionBuilder(TestActionBuilder<?> builder) {
        if (builder instanceof DelegatingTestActionBuilder) {
            return resolveActionBuilder(((DelegatingTestActionBuilder<?>) builder).getDelegate());
        }
        return builder;
    }

    /**
     * Adds actions to the container.
     * @param container
     * @return
     */
    protected T build(T container) {
        container.setActions(actions.stream()
                .map(TestActionBuilder::build)
                .filter(action -> !(action instanceof NoopTestAction))
                .collect(Collectors.toList()));
        return container;
    }

    @Override
    public List<TestActionBuilder<?>> getActions() {
        return actions;
    }
}
