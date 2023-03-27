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

package org.citrusframework.citrus.yaml.actions;

import java.util.List;

import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.container.Template;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;
import org.citrusframework.citrus.spi.SimpleReferenceResolver;
import org.citrusframework.citrus.yaml.TemplateLoader;

/**
 * @author Christoph Deppisch
 */
public class ApplyTemplate implements TestActionBuilder<Template>, ReferenceResolverAware {

    private final Template.Builder builder = new Template.Builder();

    private String filePath;
    private ReferenceResolver referenceResolver;

    public void setName(String name) {
        builder.templateName(name);
    }

    public void setFile(String filePath) {
        this.filePath = filePath;
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
        if (filePath != null) {
            Template local = new TemplateLoader(filePath)
                    .withReferenceResolver(referenceResolver)
                    .load()
                    .build();

            SimpleReferenceResolver temporaryReferenceResolver = new SimpleReferenceResolver();
            temporaryReferenceResolver.bind(local.getTemplateName(), local);

            builder.withReferenceResolver(temporaryReferenceResolver);
            builder.templateName(local.getTemplateName());
        } else {
            builder.withReferenceResolver(referenceResolver);
        }

        return builder.build();
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
