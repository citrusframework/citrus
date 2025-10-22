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

package org.citrusframework.xml.container;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.TestActions;

@XmlRootElement(name = "timer")
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

    @XmlElement
    public void setDescription(String value) {
        builder.description(value);
    }

    @XmlAttribute
    public void setId(String id) {
        builder.timerId(id);
    }

    @XmlAttribute
    public void setDelay(long milliseconds) {
        builder.delay(milliseconds);
    }

    @XmlAttribute
    public void setFork(boolean enabled) {
        builder.fork(enabled);
    }

    @XmlAttribute(name = "auto-stop")
    public void setAutoStop(boolean enabled) {
        builder.autoStop(enabled);
    }

    @XmlAttribute
    public void setInterval(long milliseconds) {
        builder.interval(milliseconds);
    }

    @XmlAttribute
    public void setRepeatCount(int count) {
        builder.repeatCount(count);
    }

    @XmlElement(required = true)
    public void setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
