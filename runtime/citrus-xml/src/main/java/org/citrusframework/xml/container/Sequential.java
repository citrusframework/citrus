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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.container.Sequence;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.xml.TestActions;

/**
 * @author Christoph Deppisch
 */
@XmlRootElement(name = "sequential")
public class Sequential implements TestActionBuilder<Sequence>, ReferenceResolverAware {

    private final Sequence.Builder builder = new Sequence.Builder();

    private ReferenceResolver referenceResolver;

    @Override
    public Sequence build() {
        builder.getActions().stream()
                .filter(action -> action instanceof ReferenceResolverAware)
                .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));

        return builder.build();
    }

    @XmlElement
    public Sequential setDescription(String value) {
        builder.description(value);
        return this;
    }

    @XmlElement
    public Sequential setActions(TestActions actions) {
        builder.actions(actions.getActionBuilders().toArray(TestActionBuilder<?>[]::new));
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
