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

package org.citrusframework.yaml.actions;

import java.util.concurrent.TimeUnit;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.yaml.SchemaProperty;

public class Sleep implements TestActionBuilder<SleepAction> {

    protected final SleepAction.Builder builder = new SleepAction.Builder();

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    public void setTime(String time) {
        builder.milliseconds(time);
    }

    @SchemaProperty(description = "Time to sleep in milliseconds.")
    public void setMilliseconds(String milliseconds) {
        builder.time(milliseconds, TimeUnit.MILLISECONDS);
    }

    @SchemaProperty(description = "Time to sleep in seconds.")
    public void setSeconds(String seconds) {
        builder.time(seconds, TimeUnit.SECONDS);
    }

    @Override
    public SleepAction build() {
        return builder.build();
    }
}
