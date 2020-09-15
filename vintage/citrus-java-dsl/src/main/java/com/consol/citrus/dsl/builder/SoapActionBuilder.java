package com.consol.citrus.dsl.builder;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.spi.ReferenceResolverAware;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.server.WebServiceServer;

/**
 * @author Christoph Deppisch
 */
public class SoapActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final com.consol.citrus.ws.actions.SoapActionBuilder delegate = new com.consol.citrus.ws.actions.SoapActionBuilder();

    public SoapClientActionBuilder client(WebServiceClient soapClient) {
        return new SoapClientActionBuilder(delegate.client(soapClient));
    }

    public SoapClientActionBuilder client(String soapClient) {
        return new SoapClientActionBuilder(delegate.client(soapClient));
    }

    public SoapServerActionBuilder server(WebServiceServer soapServer) {
        return new SoapServerActionBuilder(delegate.server(soapServer));
    }

    public SoapServerActionBuilder server(String soapServer) {
        return new SoapServerActionBuilder(delegate.server(soapServer));
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

    public static class SoapServerActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {
        private final com.consol.citrus.ws.actions.SoapServerActionBuilder delegate;

        public SoapServerActionBuilder(com.consol.citrus.ws.actions.SoapServerActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Generic request builder for receiving SOAP messages on server.
         * @return
         */
        public ReceiveSoapMessageActionBuilder receive() {
            return new ReceiveSoapMessageActionBuilder(delegate.receive());
        }

        /**
         * Generic response builder for sending SOAP response messages to client.
         * @return
         */
        public SendSoapMessageActionBuilder send() {
            return new SendSoapMessageActionBuilder(delegate.send());
        }

        /**
         * Generic response builder for sending SOAP fault messages to client.
         * @return
         */
        public SendSoapFaultActionBuilder sendFault() {
            return new SendSoapFaultActionBuilder(delegate.sendFault());
        }

        /**
         * Sets the Spring bean application context.
         * @param referenceResolver
         */
        public SoapServerActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.withReferenceResolver(referenceResolver);
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.setReferenceResolver(referenceResolver);
        }

        @Override
        public TestAction build() {
            return delegate.build();
        }

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate.getDelegate();
        }
    }

    public static class SoapClientActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {
        private final com.consol.citrus.ws.actions.SoapClientActionBuilder delegate;

        public SoapClientActionBuilder(com.consol.citrus.ws.actions.SoapClientActionBuilder delegate) {
            this.delegate = delegate;
        }

        /**
         * Generic response builder for expecting response messages on client.
         * @return
         */
        public ReceiveSoapMessageActionBuilder receive() {
            return new ReceiveSoapMessageActionBuilder(delegate.receive());
        }

        /**
         * Generic request builder with request method and path.
         * @return
         */
        public SendSoapMessageActionBuilder send() {
            return new SendSoapMessageActionBuilder(delegate.send());
        }

        /**
         * Sets the bean reference resolver.
         * @param referenceResolver
         */
        public SoapClientActionBuilder withReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.withReferenceResolver(referenceResolver);
            return this;
        }

        @Override
        public void setReferenceResolver(ReferenceResolver referenceResolver) {
            delegate.setReferenceResolver(referenceResolver);
        }

        @Override
        public TestAction build() {
            return delegate.build();
        }

        @Override
        public TestActionBuilder<?> getDelegate() {
            return delegate.getDelegate();
        }
    }
}
