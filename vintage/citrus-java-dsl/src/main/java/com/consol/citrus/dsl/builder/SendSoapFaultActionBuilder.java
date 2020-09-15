package com.consol.citrus.dsl.builder;

import java.nio.charset.Charset;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultActionBuilder extends SendMessageActionBuilder<SendSoapFaultActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<SendMessageAction> {

    private final SendSoapFaultAction.Builder delegate;

    public SendSoapFaultActionBuilder(SendSoapFaultAction.Builder builder) {
        super(builder);
        this.delegate = builder;
    }

    /**
     * Adds custom SOAP fault code.
     * @param code
     * @return
     */
    public SendSoapFaultActionBuilder faultCode(String code) {
        delegate.faultCode(code);
        return this;
    }

    /**
     * Add custom fault string to SOAP fault message.
     * @param faultString
     * @return
     */
    public SendSoapFaultActionBuilder faultString(String faultString) {
        delegate.faultString(faultString);
        return this;
    }

    /**
     * Add custom fault string to SOAP fault message.
     * @param faultActor
     * @return
     */
    public SendSoapFaultActionBuilder faultActor(String faultActor) {
        delegate.faultActor(faultActor);
        return this;
    }

    /**
     * Adds a fault detail to SOAP fault message.
     * @param faultDetail
     * @return
     */
    public SendSoapFaultActionBuilder faultDetail(String faultDetail) {
        delegate.faultDetail(faultDetail);
        return this;
    }

    /**
     * Adds a fault detail from file resource.
     * @param resource
     * @return
     */
    public SendSoapFaultActionBuilder faultDetailResource(Resource resource) {
        return faultDetailResource(resource, FileUtils.getDefaultCharset());
    }

    /**
     * Adds a fault detail from file resource.
     * @param resource
     * @param charset
     * @return
     */
    public SendSoapFaultActionBuilder faultDetailResource(Resource resource, Charset charset) {
        delegate.faultDetailResource(resource, charset);
        return this;
    }

    /**
     * Adds a fault detail from file resource path.
     * @param filePath
     * @return
     */
    public SendSoapFaultActionBuilder faultDetailResource(String filePath) {
        delegate.faultDetailResource(filePath);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public SendSoapFaultActionBuilder status(HttpStatus status) {
        delegate.status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public SendSoapFaultActionBuilder statusCode(Integer statusCode) {
        delegate.statusCode(statusCode);
        return this;
    }

    @Override
    public SendMessageAction build() {
        return delegate.build();
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
