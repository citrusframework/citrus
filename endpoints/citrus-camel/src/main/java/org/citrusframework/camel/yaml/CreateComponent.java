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
import org.citrusframework.yaml.SchemaProperty;

public class CreateComponent implements CamelActionBuilderWrapper<CreateCamelComponentAction.Builder> {
    private final CreateCamelComponentAction.Builder builder = new CreateCamelComponentAction.Builder();

    @SchemaProperty(description = "The Camel component name.")
    public void setName(String componentName) {
        builder.componentName(componentName);
    }

    @SchemaProperty(description = "The Camel component script loaded from a file resource.")
    public void setFile(String file) {
        builder.component(Resources.create(file));
    }

    @SchemaProperty(description = "The script that defines the Camel component.")
    public void setScript(String script) {
        builder.component(script);
    }

    @Override
    public CreateCamelComponentAction.Builder getBuilder() {
        return builder;
    }
}
