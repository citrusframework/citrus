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

package org.citrusframework.camel.actions;

import jakarta.annotation.Nullable;
import org.apache.camel.CamelContext;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.camel.actions.infra.CamelInfraActionBuilder;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public class CamelActionBuilder implements
        TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware,
        org.citrusframework.actions.camel.CamelActionBuilder<TestAction, CamelActionBuilder> {

    private CamelContext camelContext;

    private TestActionBuilder<?> delegate;

    /**
     * Entrance for all Camel related Java DSL functionalities.
     */
    public CamelActionBuilder camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    @Override
    public CamelActionBuilder camelContext(Object o) {
        if (o instanceof CamelContext context) {
            this.camelContext = context;
        } else {
            throw new CitrusRuntimeException("Expected a CamelContext, but got %s".formatted(o.getClass().getName()));
        }

        return this;
    }

    @Override
    public CamelContextActionBuilder camelContext() {
        CamelContextActionBuilder builder = new CamelContextActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelExchangeActionBuilder<SendMessageAction.Builder> send() {
        CamelExchangeActionBuilder<SendMessageAction.Builder> builder = CamelExchangeActionBuilder.send();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelExchangeActionBuilder<ReceiveMessageAction.Builder> receive() {
        CamelExchangeActionBuilder<ReceiveMessageAction.Builder> builder = CamelExchangeActionBuilder.receive();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CreateCamelComponentAction.Builder bind(String name, Object component) {
        CreateCamelComponentAction.Builder builder = CreateCamelComponentAction.Builder.bind()
                .context(camelContext)
                .component(name, component);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CreateCamelComponentAction.Builder bind() {
        CreateCamelComponentAction.Builder builder = CreateCamelComponentAction.Builder.bind()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelControlBusAction.Builder controlBus() {
        CamelControlBusAction.Builder builder = CamelControlBusAction.Builder.controlBus()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelRouteActionBuilder route() {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelInfraActionBuilder infra() {
        CamelInfraActionBuilder builder = new CamelInfraActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelJBangActionBuilder jbang() {
        CamelJBangActionBuilder builder = new CamelJBangActionBuilder();
        this.delegate = builder;
        return builder;
    }

    @Override
    public CamelActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     */
    @Override
    public void setReferenceResolver(@Nullable ReferenceResolver referenceResolver) {
        if (referenceResolver != null
                && delegate instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(referenceResolver);
        }
    }
}
