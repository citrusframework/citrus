/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.camel.yaml;

import org.apache.camel.CamelContext;
import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class Camel implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private CamelActionBuilderWrapper<?> delegate;

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

    public void setCreateComponent(CreateComponent builder) {
        this.delegate = builder;
    }

    public void setCreateContext(CreateContext builder) {
        this.delegate = builder;
    }

    public void setStartContext(StartContext builder) {
        this.delegate = builder;
    }

    public void setStopContext(StopContext builder) {
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

    public void setJbang(JBang builder) {
        this.delegate = builder;
    }

    @Override
    public TestAction build() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Camel action - please provide proper action details");
        }

        AbstractTestActionBuilder<?, ?> builder = delegate.getBuilder();

        if (builder instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }

        builder.description(description);

        if (referenceResolver != null) {
            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }

            if (camelContext != null && builder instanceof AbstractCamelAction.Builder<?, ?> camelActionBuilder) {
                camelActionBuilder.context(referenceResolver.resolve(camelContext, CamelContext.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
