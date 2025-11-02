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

import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Template;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;
import org.citrusframework.yaml.YamlTemplateLoader;

public class ApplyTemplate implements TestActionBuilder<Template>, ReferenceResolverAware {

    private final Template.Builder builder = new Template.Builder();

    @SchemaProperty(required = true, description = "The name of the template.")
    public void setName(String name) {
        builder.templateName(name);
    }

    public void setTemplateName(String name) {
        builder.templateName(name);
    }

    @SchemaProperty(description = "Load the template from given file resource.")
    public void setFile(String filePath) {
        builder.file(filePath);
        builder.loader(new YamlTemplateLoader());
    }

    @SchemaProperty(description = "Template parameters, passed to the template as test variables.")
    public void setParameters(List<Parameter> parameters) {
        parameters.forEach(p -> {
            if (p.value != null) {
                builder.parameter(p.name, p.value.trim());
            }
        });
    }

    @Override
    public Template build() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
    }

    public static class Parameter {

        protected String name;
        protected String value = "";

        public String getName() {
            return name;
        }

        @SchemaProperty(required = true, description = "The name of the parameter.")
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty(required = true, description = "The parameter value.")
        public void setValue(String value) {
            this.value = value;
        }
    }
}
