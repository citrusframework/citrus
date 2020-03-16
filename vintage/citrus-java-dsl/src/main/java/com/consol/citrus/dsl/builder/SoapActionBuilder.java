package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.ws.actions.SoapClientActionBuilder;
import com.consol.citrus.ws.actions.SoapServerActionBuilder;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;

/**
 * @author Christoph Deppisch
 */
public class SoapActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final com.consol.citrus.ws.actions.SoapActionBuilder delegate = new com.consol.citrus.ws.actions.SoapActionBuilder();

    public SoapClientActionBuilder client(WebServiceClient soapClient) {
        return delegate.client(soapClient);
    }

    public SoapClientActionBuilder client(String soapClient) {
        return delegate.client(soapClient);
    }

    public SoapServerActionBuilder server(WebServiceServer soapServer) {
        return delegate.server(soapServer);
    }

    public SoapServerActionBuilder server(String soapServer) {
        return delegate.server(soapServer);
    }

    public SoapActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.withReferenceResolver(referenceResolver);
        return this;
    }

    @Override
    public TestAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
