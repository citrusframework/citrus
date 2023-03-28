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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.TestActions;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "conditional")
public class Conditional implements TestActionBuilder<org.citrusframework.container.Conditional>, ReferenceResolverAware {

    private final org.citrusframework.container.Conditional.Builder builder = new org.citrusframework.container.Conditional.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public org.citrusframework.container.Conditional build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @XmlElement
    public Conditional setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute(name = "when", required = true)
    public Conditional setWhen(String expression) {
        builder.when(expression);
        return this;
    }

    @XmlElement
    public Conditional setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
