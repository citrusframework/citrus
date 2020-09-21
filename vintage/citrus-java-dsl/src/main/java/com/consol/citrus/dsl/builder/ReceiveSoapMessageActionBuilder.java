package com.consol.citrus.dsl.builder;

import java.nio.charset.Charset;

import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageActionBuilder extends ReceiveMessageActionBuilder<ReceiveSoapMessageActionBuilder>
        implements TestActionBuilder.DelegatingTestActionBuilder<ReceiveMessageAction> {

    private final ReceiveSoapMessageAction.Builder delegate;

    public ReceiveSoapMessageActionBuilder(ReceiveSoapMessageAction.Builder builder) {
        super(builder);
        this.delegate = builder;
    }

    /**
     * Sets special SOAP action message header.
     * @param soapAction
     * @return
     */
    public ReceiveSoapMessageActionBuilder soapAction(String soapAction) {
        delegate.soapAction(soapAction);
        return this;
    }

    /**
     * Sets the control attachment with string content.
     * @param contentId
     * @param contentType
     * @param content
     * @return
     */
    public ReceiveSoapMessageActionBuilder attachment(String contentId, String contentType, String content) {
        delegate.attachment(contentId, contentType, content);
        return this;
    }

    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @return
     */
    public ReceiveSoapMessageActionBuilder attachment(String contentId, String contentType, Resource contentResource) {
        return attachment(contentId, contentType, contentResource, FileUtils.getDefaultCharset());
    }

    /**
     * Sets the control attachment with content resource.
     * @param contentId
     * @param contentType
     * @param contentResource
     * @param charset
     * @return
     */
    public ReceiveSoapMessageActionBuilder attachment(String contentId, String contentType, Resource contentResource, Charset charset) {
        delegate.attachment(contentId, contentType, contentResource, charset);
        return this;
    }

    /**
     * Sets the charset name for this send action builder's control attachment.
     * @param charsetName
     * @return
     */
    public ReceiveSoapMessageActionBuilder charset(String charsetName) {
        delegate.charset(charsetName);
        return this;
    }

    /**
     * Sets the control attachment from Java object instance.
     * @param attachment
     * @return
     */
    public ReceiveSoapMessageActionBuilder attachment(SoapAttachment attachment) {
        delegate.attachment(attachment);
        return this;
    }

    /**
     * Set explicit SOAP attachment validator.
     * @param validator
     * @return
     */
    public ReceiveSoapMessageActionBuilder attachmentValidator(SoapAttachmentValidator validator) {
        delegate.attachmentValidator(validator);
        return this;
    }

    /**
     * Sets the request content type header.
     * @param contentType
     * @return
     */
    public ReceiveSoapMessageActionBuilder contentType(String contentType) {
        delegate.contentType(contentType);
        return this;
    }

    /**
     * Sets the request accept header.
     * @param accept
     * @return
     */
    public ReceiveSoapMessageActionBuilder accept(String accept) {
        delegate.accept(accept);
        return this;
    }

    /**
     * Sets the response status.
     * @param status
     * @return
     */
    public ReceiveSoapMessageActionBuilder status(HttpStatus status) {
        delegate.status(status);
        return this;
    }

    /**
     * Sets the response status code.
     * @param statusCode
     * @return
     */
    public ReceiveSoapMessageActionBuilder statusCode(Integer statusCode) {
        delegate.statusCode(statusCode);
        return this;
    }

    /**
     * Sets the context path.
     * @param contextPath
     * @return
     */
    public ReceiveSoapMessageActionBuilder contextPath(String contextPath) {
        delegate.contextPath(contextPath);
        return this;
    }

    @Override
    public TestActionBuilder<?> getDelegate() {
        return delegate;
    }
}
