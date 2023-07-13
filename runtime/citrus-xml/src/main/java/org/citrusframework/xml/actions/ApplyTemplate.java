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

package org.citrusframework.xml.actions;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Template;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.XmlTemplateLoader;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "apply-template")
public class ApplyTemplate implements TestActionBuilder<Template>, ReferenceResolverAware {

    private final Template.Builder builder = new Template.Builder();

    @XmlAttribute
    public ApplyTemplate setName(String name) {
        builder.templateName(name);
        return this;
    }

    @XmlAttribute
    public void setTemplateName(String name) {
        builder.templateName(name);
    }

    @XmlAttribute
    public ApplyTemplate setFile(String filePath) {
        builder.file(filePath);
        builder.loader(new XmlTemplateLoader());
        return this;
    }

    @XmlElement
    public ApplyTemplate setParameters(Parameters parameters) {
        parameters.getParameters().forEach(p -> {
            if (p.multilineValue != null) {
                builder.parameter(p.name, p.multilineValue);
            } else if (p.value != null) {
                builder.parameter(p.name, p.value);
            }
        });
        return this;
    }

    @Override
    public Template build() {
        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        builder.setReferenceResolver(referenceResolver);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "parameters"
    })
    public static class Parameters {
        @XmlElement(name = "parameter")
        protected List<Parameter> parameters;

        public List<Parameter> getParameters() {
            if (parameters == null) {
                parameters = new ArrayList<>();
            }
            return this.parameters;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Parameter {

            @XmlElement(name = "value")
            protected String multilineValue;
            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute
            protected String value = "";

            public String getMultilineValue() {
                return multilineValue;
            }

            public void setMultilineValue(String value) {
                this.multilineValue = value;
            }

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
}
