/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.ws.actions;

import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;
import org.springframework.util.Assert;

/**
 * Action executes soap client operations such as sending requests and receiving responses.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapClientActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target soap client instance */
    private Endpoint soapClient;
    private String soapClientUri;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public SoapClientActionBuilder(Endpoint soapClient) {
        this.soapClient = soapClient;
    }

    /**
     * Default constructor.
     */
    public SoapClientActionBuilder(String soapClientUri) {
        this.soapClientUri = soapClientUri;
    }

    /**
     * Generic response builder for expecting response messages on client.
     * @return
     */
    public ReceiveSoapMessageAction.Builder receive() {
        ReceiveSoapMessageAction.Builder builder = new ReceiveSoapMessageAction.Builder();
        if (soapClient != null) {
            builder.endpoint(soapClient);
        } else {
            builder.endpoint(soapClientUri);
        }

        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Generic request builder with request method and path.
     * @return
     */
    public SendSoapMessageAction.Builder send() {
        SendSoapMessageAction.Builder builder = new SendSoapMessageAction.Builder();
        if (soapClient != null) {
            builder.endpoint(soapClient);
        } else {
            builder.endpoint(soapClientUri);
        }

        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the bean reference resolver.
     * @param referenceResolver
     */
    public SoapClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        Assert.notNull(delegate, "Missing delegate action to build");
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }

    /**
     * Specifies the referenceResolver.
     * @param referenceResolver
     */
    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        if (referenceResolver == null) {
            this.referenceResolver = referenceResolver;

            if (delegate instanceof ReferenceResolverAware) {
                ((ReferenceResolverAware) delegate).setReferenceResolver(referenceResolver);
            }
        }
    }
}
