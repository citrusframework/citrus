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

package org.citrusframework.ws.actions;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.ObjectHelper;

/**
 * Action executes soap server operations such as receiving requests and sending response messsages.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class SoapServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target soap client instance */
    private Endpoint soapServer;
    private String soapServerUri;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public SoapServerActionBuilder(Endpoint soapServer) {
        this.soapServer = soapServer;
    }

    /**
     * Default constructor.
     */
    public SoapServerActionBuilder(String soapServerUri) {
        this.soapServerUri = soapServerUri;
    }

    /**
     * Generic request builder for receiving SOAP messages on server.
     * @return
     */
    public ReceiveSoapMessageAction.Builder receive() {
        ReceiveSoapMessageAction.Builder builder = new ReceiveSoapMessageAction.Builder();
        if (soapServer != null) {
            builder.endpoint(soapServer);
        } else {
            builder.endpoint(soapServerUri);
        }

        builder.name("soap:receive-request");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Generic response builder for sending SOAP response messages to client.
     * @return
     */
    public SendSoapMessageAction.Builder send() {
        SendSoapMessageAction.Builder builder = new SendSoapMessageAction.Builder();
        if (soapServer != null) {
            builder.endpoint(soapServer);
        } else {
            builder.endpoint(soapServerUri);
        }

        builder.name("soap:send-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    /**
     * Generic response builder for sending SOAP fault messages to client.
     * @return
     */
    public SendSoapFaultAction.Builder sendFault() {
        SendSoapFaultAction.Builder builder = new SendSoapFaultAction.Builder();
        if (soapServer != null) {
            builder.endpoint(soapServer);
        } else {
            builder.endpoint(soapServerUri);
        }

        builder.name("soap:send-fault");
        builder.withReferenceResolver(referenceResolver);
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
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");
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
