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

import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.StopTimerAction;

public class StopTimer implements TestActionBuilder<StopTimerAction> {

    private final StopTimerAction.Builder builder = new StopTimerAction.Builder();

    public void setId(String id) {
        builder.id(id);
    }

    public void setDescription(String value) {
        builder.description(value);
    }

    @Override
    public StopTimerAction build() {
        return builder.build();
    }
}
