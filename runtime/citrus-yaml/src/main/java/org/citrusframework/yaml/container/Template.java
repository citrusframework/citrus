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
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.TestActions;

public class Template implements TestActionBuilder<org.citrusframework.container.Template>, ReferenceResolverAware {

    private final org.citrusframework.container.Template.Builder builder = new org.citrusframework.container.Template.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public org.citrusframework.container.Template build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        builder.description(value);
    }

    @SchemaProperty(required = true, description = "Template name.")
    public void setName(String name) {
        builder.name(String.format("template:%s", name));
        builder.templateName(name);
    }

    @SchemaProperty(advanced = true, description = "Sets global scope for test variables.")
    public void setGlobalContext(boolean globalContext) {
        builder.globalContext(globalContext);
    }

    @SchemaProperty(description = "Optional set of template parameters that get set as test variables.")
    public void setParameters(List<Parameter> parameters) {
        parameters.forEach(p -> {
            if (p.value != null) {
                builder.parameter(p.name, p.value.trim());
            }
        });
    }

    @SchemaProperty(required = true, description = "Sequence of test actions to execute.")
    public void setActions(List<TestActions> actions) {
        builder.actions(actions.stream().map(TestActions::get).toArray(TestActionBuilder<?>[]::new));
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public static class Parameter {
        protected String name;
        protected String value = "";

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true, description = "Name of the parameter.")
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The parameter value")
        public void setValue(String value) {
            this.value = value;
        }
    }
}
