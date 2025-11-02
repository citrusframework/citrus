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
import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.yaml.SchemaProperty;

public class StopTime implements TestActionBuilder<StopTimeAction> {

    private final StopTimeAction.Builder builder = new StopTimeAction.Builder();

    @SchemaProperty(description = "The time line identifier.")
    public void setId(String id) {
        builder.id(id);
    }

    @SchemaProperty(description = "Suffix added to the value test variable.")
    public void setSuffix(String suffix) {
        builder.suffix(suffix);
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    @Override
    public StopTimeAction build() {
        return builder.build();
    }
}
