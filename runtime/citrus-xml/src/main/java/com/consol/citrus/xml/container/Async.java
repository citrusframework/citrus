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

package com.consol.citrus.xml.container;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.xml.TestActions;

@XmlRootElement(name = "async")
public class Async implements TestActionBuilder<com.consol.citrus.container.Async>, ReferenceResolverAware {

    private final com.consol.citrus.container.Async.Builder builder = new com.consol.citrus.container.Async.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public com.consol.citrus.container.Async build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @XmlElement
    public Async setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement
    public Async setActions(TestActions actions) {
        builder.actions(actions.getActions().stream()
                .filter(t -> t instanceof TestActionBuilder<?>)
                .map(TestActionBuilder.class::cast)
                .toArray(TestActionBuilder<?>[]::new));

        return this;
    }

    @XmlElement
    public Async setSuccess(TestActions actions) {
        builder.successActions(actions.getActions().stream()
                .filter(t -> t instanceof TestActionBuilder<?>)
                .map(TestActionBuilder.class::cast)
                .toArray(TestActionBuilder<?>[]::new));

        return this;
    }

    @XmlElement
    public Async setError(TestActions actions) {
        builder.errorActions(actions.getActions().stream()
                .filter(t -> t instanceof TestActionBuilder<?>)
                .map(TestActionBuilder.class::cast)
                .toArray(TestActionBuilder<?>[]::new));

        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
