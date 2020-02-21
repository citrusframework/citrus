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

package com.consol.citrus.ws.actions;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.endpoint.Endpoint;
import org.springframework.util.Assert;

/**
 * Action executes soap server operations such as receiving requests and sending response messsages.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction> {

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target soap client instance */
    private final Endpoint soapServer;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public SoapServerActionBuilder(Endpoint soapServer) {
        this.soapServer = soapServer;
    }

    /**
     * Generic request builder for receiving SOAP messages on server.
     * @return
     */
    public ReceiveSoapMessageAction.Builder receive() {
        ReceiveSoapMessageAction.Builder builder = new ReceiveSoapMessageAction.Builder()
                .endpoint(soapServer)
                .withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Generic response builder for sending SOAP response messages to client.
     * @return
     */
    public SendSoapMessageAction.Builder send() {
        SendSoapMessageAction.Builder builder = new SendSoapMessageAction.Builder()
                .endpoint(soapServer)
                .withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Generic response builder for sending SOAP fault messages to client.
     * @return
     */
    public SendSoapFaultAction.Builder sendFault() {
        SendSoapFaultAction.Builder builder = new SendSoapFaultAction.Builder()
                .endpoint(soapServer)
                .withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Sets the Spring bean application context.
     * @param referenceResolver
     */
    public SoapServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
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
}
