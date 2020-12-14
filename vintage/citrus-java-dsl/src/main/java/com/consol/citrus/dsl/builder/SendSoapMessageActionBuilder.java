package com.consol.citrus.dsl.builder;

import java.nio.charset.Charset;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import org.springframework.core.io.Resource;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionBuilder extends SendMessageActionBuilder<SendSoapMessageActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<SendMessageAction> {

    private final SendSoapMessageAction.Builder delegate;

    public SendSoapMessageActionBuilder(SendSoapMessageAction.Builder builder) {
        super(builder);
        this.delegate = builder;
    }

    /**
     * Sets special SOAP action message header.
     * @param soapAction
     * @return
     */
    public SendSoapMessageActionBuilder soapAction(String soapAction) {
        delegate.message().soapAction(soapAction);
        return this;
    }

    /**
     * Sets the attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public SendSoapMessageActionBuilder attachment(String contentId, String contentType, String content) {
        delegate.message().attachment(contentId, contentType, content);
        return this;
    }

    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public SendSoapMessageActionBuilder attachment(String contentId, String contentType, Resource contentResource) {
        return attachment(contentId, contentType, contentResource, FileUtils.getDefaultCharset());
    }

    /**
     * Sets the attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @param charset
     * @return
     */
    public SendSoapMessageActionBuilder attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
        delegate.message().attachment(contentId, contentType, contentResource, charset);
        return this;
    }

    /**
     * Sets the charset name for this send action builder's attachment.
     * @param charsetName
     * @return
     */
    public SendSoapMessageActionBuilder charset(String charsetName) {
        delegate.message().charset(charsetName);
        return this;
    }

    /**
     * Sets the attachment from Java object instance.
     * @param attachment
     * @return
     */
    public SendSoapMessageActionBuilder attachment(SoapAttachment attachment) {
        delegate.message().attachment(attachment);
        return this;
    }

    /**
     * Set the endpoint URI for the request. This works only if the HTTP endpoint used
     * doesn't provide an own endpoint URI resolver.
     *
     * @param uri absolute URI to use for the endpoint
     * @return self
     */
    public SendSoapMessageActionBuilder uri(String uri) {
        delegate.message().uri(uri);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public SendSoapMessageActionBuilder contentType(String contentType) {
        delegate.message().contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public SendSoapMessageActionBuilder accept(String accept) {
        delegate.message().accept(accept);
        return this;
    }

    public SendSoapMessageActionBuilder mtomEnabled(boolean mtomEnabled) {
        delegate.mtomEnabled(mtomEnabled);
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
