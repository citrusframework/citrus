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
import org.citrusframework.yaml.TestActions;

public class Timer implements TestActionBuilder<org.citrusframework.container.Timer>, ReferenceResolverAware {

    private final org.citrusframework.container.Timer.Builder builder = new org.citrusframework.container.Timer.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public org.citrusframework.container.Timer build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    public void setDescription(String value) {
        builder.description(value);
    }

    public void setId(String id) {
        builder.timerId(id);
    }

    public void setDelay(long milliseconds) {
        builder.delay(milliseconds);
    }

    public void setFork(boolean enabled) {
        builder.fork(enabled);
    }

    public void setAutoStop(boolean enabled) {
        builder.autoStop(enabled);
    }

    public void setInterval(long milliseconds) {
        builder.interval(milliseconds);
    }

    public void setRepeatCount(int count) {
        builder.repeatCount(count);
    }

    public void setActions(List<TestActions> actions) {
        builder.actions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
