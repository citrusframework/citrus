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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.camel.endpoint.CamelEndpoint;
import org.citrusframework.endpoint.EndpointUriBuilder;
import org.citrusframework.message.builder.MessageBuilderSupport;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

/**
 * Action builder.
 */
public class CamelExchangeActionBuilder<T extends MessageBuilderSupport.MessageActionBuilder<?, ?, ?>> implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private T delegate;

    private ReferenceResolver referenceResolver;

    static CamelExchangeActionBuilder<SendMessageAction.Builder> send() {
        CamelExchangeActionBuilder<SendMessageAction.Builder> instance = new CamelExchangeActionBuilder<>();
        instance.delegate = SendMessageAction.Builder.send();
        return instance;
    }

    static CamelExchangeActionBuilder<ReceiveMessageAction.Builder> receive() {
        CamelExchangeActionBuilder<ReceiveMessageAction.Builder> instance = new CamelExchangeActionBuilder<>();
        instance.delegate = ReceiveMessageAction.Builder.receive();
        return instance;
    }

    public T endpoint(CamelEndpoint endpoint) {
        delegate.endpoint(endpoint);
        return delegate;
    }

    public T endpoint(EndpointUriBuilder builder) {
        return endpoint(builder.getUri());
    }

    public T endpoint(String endpointUri) {
        return endpoint(endpointUri, false);
    }

    public T endpoint(String endpointUri, boolean inOut) {
        String prefix;
        if (inOut) {
            prefix = "camel:sync:";
        } else {
            prefix = "camel:";
        }

        if (endpointUri.startsWith("camel:")) {
            delegate.endpoint(endpointUri);
        } else {
            delegate.endpoint(prefix + endpointUri);
        }
        return delegate;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public CamelExchangeActionBuilder<T> withReferenceResolver(ReferenceResolver referenceResolver) {
        setReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        if (delegate != null) {
            delegate.setReferenceResolver(referenceResolver);
        }
    }

    @Override
    public T getDelegate() {
        return delegate;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
        delegate.setReferenceResolver(referenceResolver);
        return delegate.build();
    }
}
