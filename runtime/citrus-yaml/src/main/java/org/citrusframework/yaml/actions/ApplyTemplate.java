/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import org.citrusframework.yaml.YamlTemplateLoader;

/**
 * @author Christoph Deppisch
 */
public class ApplyTemplate implements TestActionBuilder<Template>, ReferenceResolverAware {

    private final Template.Builder builder = new Template.Builder();

    public void setName(String name) {
        builder.templateName(name);
    }

    public void setTemplateName(String name) {
        builder.templateName(name);
    }

    public void setFile(String filePath) {
        builder.file(filePath);
        builder.loader(new YamlTemplateLoader());
    }

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

        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
