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
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

public class CamelActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private CamelContext camelContext;

    private TestActionBuilder<?> delegate;

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public CamelActionBuilder camelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        return this;
    }

    /**
     * Static entrance for all Camel related Java DSL functionalities.
     * @return
     */
    public CamelContextActionBuilder camelContext() {
        CamelContextActionBuilder builder = new CamelContextActionBuilder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Sends message using Camel endpointUri and components.
     * @return
     */
    public CamelExchangeActionBuilder<SendMessageAction.Builder> send() {
        CamelExchangeActionBuilder<SendMessageAction.Builder> builder = CamelExchangeActionBuilder.send();
        this.delegate = builder;
        return builder;
    }

    /**
     * Receive messages using Camel endpointUri and components.
     * @return
     */
    public CamelExchangeActionBuilder<ReceiveMessageAction.Builder> receive() {
        CamelExchangeActionBuilder<ReceiveMessageAction.Builder> builder = CamelExchangeActionBuilder.receive();
        this.delegate = builder;
        return builder;
    }

    /**
     * Binds given component to the Camel context.
     * @return
     */
    public CreateCamelComponentAction.Builder bind(String name, Object component) {
        CreateCamelComponentAction.Builder builder = CreateCamelComponentAction.Builder.bind()
                .context(camelContext)
                .component(name, component);
        this.delegate = builder;
        return builder;
    }

    /**
     * Binds a component to the Camel context.
     * @return
     */
    public CreateCamelComponentAction.Builder bind() {
        CreateCamelComponentAction.Builder builder = CreateCamelComponentAction.Builder.bind()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    /**
     * Creates new control bus test action builder and sets the Camel context.
     * @return
     */
    public CamelControlBusAction.Builder controlBus() {
        CamelControlBusAction.Builder builder = CamelControlBusAction.Builder.controlBus()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    /**
     * Perform actions on a Camel route such as start/stop/create and process.
     * @return
     */
    public CamelRouteActionBuilder route() {
        CamelRouteActionBuilder builder = new CamelRouteActionBuilder()
                .context(camelContext);
        this.delegate = builder;
        return builder;
    }

    /**
     * Perform actions with Camel JBang.
     * @return
     */
    public CamelJBangActionBuilder jbang() {
        CamelJBangActionBuilder builder = new CamelJBangActionBuilder();
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
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
