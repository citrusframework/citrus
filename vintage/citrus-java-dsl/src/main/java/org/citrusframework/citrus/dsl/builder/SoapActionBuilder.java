package org.citrusframework.citrus.dsl.builder;

import org.citrusframework.citrus.TestAction;
import org.citrusframework.citrus.TestActionBuilder;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.spi.ReferenceResolverAware;
import org.citrusframework.citrus.ws.client.WebServiceClient;
import org.citrusframework.citrus.ws.server.WebServiceServer;

/**
 * @author Christoph Deppisch
 */
public class SoapActionBuilder implements TestActionBuilder.DelegatingTestActionBuilder<TestAction>, ReferenceResolverAware {

    private final org.citrusframework.citrus.ws.actions.SoapActionBuilder delegate = new org.citrusframework.citrus.ws.actions.SoapActionBuilder();

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
        private final org.citrusframework.citrus.ws.actions.SoapServerActionBuilder delegate;

        public SoapServerActionBuilder(org.citrusframework.citrus.ws.actions.SoapServerActionBuilder delegate) {
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
        private final org.citrusframework.citrus.ws.actions.SoapClientActionBuilder delegate;

        public SoapClientActionBuilder(org.citrusframework.citrus.ws.actions.SoapClientActionBuilder delegate) {
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
