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

package org.citrusframework.camel.yaml;

import org.citrusframework.camel.actions.CreateCamelComponentAction;
import org.citrusframework.spi.Resources;

public class CreateComponent implements CamelActionBuilderWrapper<CreateCamelComponentAction.Builder> {
    private final CreateCamelComponentAction.Builder builder = new CreateCamelComponentAction.Builder();

    public void setName(String componentName) {
        builder.componentName(componentName);
    }

    public void setFile(String file) {
        builder.component(Resources.create(file));
    }

    public void setScript(String script) {
        builder.component(script);
    }

    @Override
    public CreateCamelComponentAction.Builder getBuilder() {
        return builder;
    }
}
