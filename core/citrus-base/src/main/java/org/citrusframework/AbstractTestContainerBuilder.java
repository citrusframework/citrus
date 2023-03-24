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

package org.citrusframework;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.citrusframework.actions.NoopTestAction;
import org.citrusframework.container.TestActionContainer;

/**
 * Abstract container builder takes care on calling the container runner when actions are placed in the container.
 * @author Christoph Deppisch
 */
public abstract class AbstractTestContainerBuilder<T extends TestActionContainer, S extends TestActionContainerBuilder<T, S>> extends AbstractTestActionBuilder<T, S> implements TestActionContainerBuilder<T, S> {

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

    @Override
    public T build() {
        T container = doBuild();

        container.setActions(actions.stream()
                .map(TestActionBuilder::build)
                .filter(action -> !(action instanceof NoopTestAction))
                .collect(Collectors.toList()));
        return container;
    }

    /**
     * Builds the container.
     * @return
     */
    protected abstract T doBuild();

    @Override
    public List<TestActionBuilder<?>> getActions() {
        return actions;
    }

    /**
     * Static Java DSL container builder using generics.
     * @param container
     * @param <T>
     * @param <B>
     * @return
     */
    public static <T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>> TestActionContainerBuilder<T, B> container(T container)  {
        return new AbstractTestContainerBuilder<T, B>() {
            @Override
            public T doBuild() {
                return container;
            }

            @Override
            public T build() {
                if (container.getActions().size() > 0) {
                    return container;
                }

                return super.build();
            }
        };
    }
}
