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
@XmlRootElement(name = "repeat")
public class Repeat implements TestActionBuilder<org.citrusframework.container.RepeatUntilTrue>, ReferenceResolverAware {

    private final org.citrusframework.container.RepeatUntilTrue.Builder builder = new org.citrusframework.container.RepeatUntilTrue.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public org.citrusframework.container.RepeatUntilTrue build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @XmlElement
    public Repeat setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlAttribute(name = "until", required = true)
    public Repeat setUntil(String condition) {
        builder.until(condition);
        return this;
    }

    @XmlAttribute
    public Repeat setIndex(String index) {
        builder.index(index);
        return this;
    }

    @XmlAttribute
    public Repeat setStartsWith(int value) {
        builder.startsWith(value);
        return this;
    }

    @XmlElement
    public Repeat setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
