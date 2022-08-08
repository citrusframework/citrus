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

package com.consol.citrus.xml.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.TraceVariablesAction;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "trace-variables")
public class TraceVariables implements TestActionBuilder<TraceVariablesAction> {

    private final TraceVariablesAction.Builder builder = new TraceVariablesAction.Builder();

    @XmlElement(name = "variable")
    protected List<Variable> variables;

    @XmlAttribute
    public TraceVariables setVariable(String variable) {
        builder.variable(variable);
        return this;
    }

    public List<Variable> getVariables() {
        if (variables == null) {
            variables = new ArrayList<>();
        }
        return this.variables;
    }

    @Override
    public TraceVariablesAction build() {
        getVariables().forEach(variable -> builder.variable(variable.name));
        return builder.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Variable {

        @XmlAttribute(name = "name", required = true)
        protected String name;

        public String getName() {
            return name;
        }
        public void setName(String value) {
            this.name = value;
        }
    }
}
