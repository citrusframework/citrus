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
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReceiveActionBuilder;
import org.citrusframework.actions.SendActionBuilder;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.spi.AbstractReferenceResolverAwareTestActionBuilder;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.util.ObjectHelper;

/**
 * Action executes soap client operations such as sending requests and receiving responses.
 *
 * @since 2.6
 */
public class SoapClientActionBuilder extends AbstractReferenceResolverAwareTestActionBuilder<TestAction>
        implements org.citrusframework.actions.ws.SoapClientActionBuilder<TestAction, SoapClientActionBuilder> {

    /** Bean reference resolver */
    private ReferenceResolver referenceResolver;

    /** Target soap client instance */
    private Endpoint soapClient;
    private String soapClientUri;

    private TestActionBuilder<?> delegate;

    /**
     * Default constructor.
     */
    public SoapClientActionBuilder() {
    }

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

    @Override
    public ReceiveSoapMessageAction.Builder receive() {
        ReceiveSoapMessageAction.Builder builder = new ReceiveSoapMessageAction.Builder();
        if (soapClient != null) {
            builder.endpoint(soapClient);
        } else {
            builder.endpoint(soapClientUri);
        }

        builder.name("soap:receive-response");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public AssertSoapFault.Builder assertFault() {
        AssertSoapFault.Builder builder = new AssertSoapFault.Builder();
        if (soapClient != null) {
            builder.endpoint(soapClient);
        } else {
            builder.endpoint(soapClientUri);
        }

        builder.name("soap:assert-fault");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SendSoapMessageAction.Builder send() {
        SendSoapMessageAction.Builder builder = new SendSoapMessageAction.Builder();
        if (soapClient != null) {
            builder.endpoint(soapClient);
        } else {
            builder.endpoint(soapClientUri);
        }

        builder.name("soap:send-request");
        builder.withReferenceResolver(referenceResolver);
        this.delegate = builder;
        return builder;
    }

    @Override
    public SoapClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
        return this;
    }

    @Override
    public TestAction build() {
        ObjectHelper.assertNotNull(delegate, "Missing delegate action to build");

        if (delegate instanceof SendActionBuilder<?, ?, ?> messageActionBuilder) {
            if (soapClient != null) {
                messageActionBuilder.endpoint(soapClient);
            } else if (soapClientUri != null) {
                messageActionBuilder.endpoint(soapClientUri);
            }
        }

        if (delegate instanceof ReceiveActionBuilder<?, ?, ?> messageActionBuilder) {
            if (soapClient != null) {
                messageActionBuilder.endpoint(soapClient);
            } else if (soapClientUri != null) {
                messageActionBuilder.endpoint(soapClientUri);
            }
        }

        return delegate.build();
    }
}
