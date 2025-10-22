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
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.TestActions;

@XmlRootElement(name = "repeat-on-error")
public class RepeatOnError implements TestActionBuilder<org.citrusframework.container.RepeatOnErrorUntilTrue>, ReferenceResolverAware {

    private final org.citrusframework.container.RepeatOnErrorUntilTrue.Builder builder = new org.citrusframework.container.RepeatOnErrorUntilTrue.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public RepeatOnErrorUntilTrue build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @XmlElement
    public void setDescription(String value) {
        builder.description(value);
    }

    @XmlAttribute(required = true)
    public void setUntil(String condition) {
        builder.until(condition);
    }

    @XmlAttribute
    public void setAutoSleep(long milliseconds) {
        builder.autoSleep(milliseconds);
    }

    @XmlAttribute(name = "auto-sleep")
    public void setAutoSleepSnakeCase(long milliseconds) {
        setAutoSleep(milliseconds);
    }

    @XmlAttribute
    public void setIndex(String index) {
        builder.index(index);
    }

    @XmlAttribute
    public void setStartsWith(int value) {
        builder.startsWith(value);
    }

    @XmlElement
    public void setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
