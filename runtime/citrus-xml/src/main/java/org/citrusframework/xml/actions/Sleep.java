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

package org.citrusframework.xml.actions;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.TimeUnit;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.SleepAction;

@XmlRootElement(name = "sleep")
public class Sleep implements TestActionBuilder<SleepAction> {

    private final SleepAction.Builder builder = new SleepAction.Builder();

    @XmlElement
    public Sleep setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute
    public Sleep setTime(String time) {
        builder.milliseconds(time);
        return this;
    }

    @XmlAttribute
    public Sleep setMilliseconds(String milliseconds) {
        builder.time(milliseconds, TimeUnit.MILLISECONDS);
        return this;
    }

    @XmlAttribute
    public Sleep setSeconds(String seconds) {
        builder.time(seconds, TimeUnit.SECONDS);
        return this;
    }

    @Override
    public SleepAction build() {
        return builder.build();
    }
}
