/*
 * Copyright 2022 the original author or authors.
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

package org.citrusframework.camel.yaml;

import org.apache.camel.CamelContext;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.camel.actions.AbstractCamelRouteAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

/**
 * @author Christoph Deppisch
 */
public class Camel implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private CamelRouteActionBuilderWrapper<?> delegate;

    private String description;
    private String actor;

    private String camelContext;

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = value;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    public void setControlBus(ControlBus builder) {
        this.delegate = builder;
    }

    public void setCreateRoutes(CreateRoutes builder) {
        this.delegate = builder;
    }

    public void setStartRoutes(StartRoutes builder) {
        this.delegate = builder;
    }

    public void setStopRoutes(StopRoutes builder) {
        this.delegate = builder;
    }

    public void setRemoveRoutes(RemoveRoutes builder) {
        this.delegate = builder;
    }

    @Override
    public TestAction build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Camel action - please provide proper action details");
        }

        AbstractCamelRouteAction.Builder<?, ?> builder = delegate.getBuilder();

        builder.setReferenceResolver(referenceResolver);
        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (camelContext != null) {
                builder.context(referenceResolver.resolve(camelContext, CamelContext.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

}
