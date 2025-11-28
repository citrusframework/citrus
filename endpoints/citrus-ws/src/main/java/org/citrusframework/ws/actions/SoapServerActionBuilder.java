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

package org.citrusframework.ws.actions;

import org.citrusframework.TestAction;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Action executes soap server operations such as receiving requests and sending response messsages.
 *
 * @since 2.6
 */
public class SoapServerActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.actions.ws.SoapServerActionBuilder<TestAction, SoapServerActionBuilder> {

    /** Target soap client instance */
    private Endpoint soapServer;
    private String soapServerUri;

    /**
     * Default constructor.
     */
    public SoapServerActionBuilder() {
    }

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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public SoapServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof SendActionBuilder<?, ?, ?> messageActionBuilder) {
            if (soapServer != null) {
                messageActionBuilder.endpoint(soapServer);
            } else if (soapServerUri != null) {
                messageActionBuilder.endpoint(soapServerUri);
            }
        }

        if (delegate instanceof ReceiveActionBuilder<?, ?, ?> messageActionBuilder) {
            if (soapServer != null) {
                messageActionBuilder.endpoint(soapServer);
            } else if (soapServerUri != null) {
                messageActionBuilder.endpoint(soapServerUri);
            }
        }

        return delegate.build();
    }
}
