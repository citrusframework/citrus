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

package org.citrusframework.xml.container;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.TestActions;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "template")
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

    @XmlElement
    public Template setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute(name = "name")
    public Template setTemplateName(String name) {
        builder.name(String.format("template:%s", name));
        builder.templateName(name);
        return this;
    }

    @XmlAttribute(name = "global-context")
    public Template setGlobalContext(boolean globalContext) {
        builder.globalContext(globalContext);
        return this;
    }

    @XmlElement
    public Template setParameters(Parameters parameters) {
        parameters.getParameters().forEach(p -> {
            if (p.multilineValue != null) {
                builder.parameter(p.name, p.multilineValue);
            } else if (p.value != null) {
                builder.parameter(p.name, p.value);
            }
        });
        return this;
    }

    @XmlElement
    public Template setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
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
