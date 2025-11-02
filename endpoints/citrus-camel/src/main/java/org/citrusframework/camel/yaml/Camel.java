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
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.yaml.SchemaProperty.Kind.ACTION;
import static org.citrusframework.yaml.SchemaProperty.Kind.GROUP;

public class Camel implements TestActionBuilder<TestAction>, ReferenceResolverAware {

    private static final String CAMEL_GROUP = "camel";
    private CamelActionBuilderWrapper<?> delegate;

    private String description;
    private String actor;
    private String camelContext;

    private ReferenceResolver referenceResolver;

    @SchemaProperty(advanced = true, description = "Test action description printed when the action is executed.")
    public void setDescription(String value) {
        this.description = value;
    }

    @SchemaProperty(advanced = true)
    public void setActor(String actor) {
        this.actor = actor;
    }

    @SchemaProperty(description = "Name of the Camel context.")
    public void setCamelContext(String camelContext) {
        this.camelContext = camelContext;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Connects with the Camel control bus to run operations.")
    public void setControlBus(ControlBus builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Create components in the Camel registry.")
    public void setCreateComponent(CreateComponent builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Create a new Camel context.")
    public void setCreateContext(CreateContext builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Starts the given Camel context.")
    public void setStartContext(StartContext builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Stops the given Camel context.")
    public void setStopContext(StopContext builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Create a new Camel route in the context.")
    public void setCreateRoutes(CreateRoutes builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Start existing Camel routes in the context.")
    public void setStartRoutes(StartRoutes builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Stop given Camel routes.")
    public void setStopRoutes(StopRoutes builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = ACTION, group = CAMEL_GROUP, description = "Remove given Camel routes.")
    public void setRemoveRoutes(RemoveRoutes builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = GROUP, group = CAMEL_GROUP, description = "Manage Camel infra services.")
    public void setInfra(Infra builder) {
        this.delegate = builder;
    }

    @SchemaProperty(kind = GROUP, group = CAMEL_GROUP, description = "Connect with Camel JBang to run commands.")
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
