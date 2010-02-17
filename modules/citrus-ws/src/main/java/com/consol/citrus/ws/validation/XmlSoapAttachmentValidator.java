package com.consol.citrus.ws.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.XmlValidationContext;
import com.consol.citrus.ws.SoapAttachment;

/**
 * Soap attachment validator delegating attachment content validation to a {@link MessageValidator}.
 * Through {@link XmlValidationContext} this class supports message validation for XML payload.
 * 
 * @author Christoph Deppisch
 */
public class XmlSoapAttachmentValidator extends AbstractSoapAttachmentValidator {
    @Autowired
    private MessageValidator validator;
    
    /** validation context holding information like expected message payload, ignored elements and so on */
    private XmlValidationContext validationContext = new XmlValidationContext();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(XmlSoapAttachmentValidator.class);

	@Override
	protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
	    if(log.isDebugEnabled()) {
            log.debug("Validating SOAP attachment content ...");
            log.debug("Received attachment content: " + StringUtils.trimWhitespace(receivedAttachment.getContent()));
            log.debug("Control attachment content: " + StringUtils.trimWhitespace(controlAttachment.getContent()));
        }
	    
	    if(receivedAttachment.getContent() != null) {
	        String controlContent = controlAttachment.getContent();
	        String receivedContent = receivedAttachment.getContent();
	        
	        Message<String> controlMessage = MessageBuilder.withPayload(controlContent).build();
	        validationContext.setExpectedMessage(controlMessage);

	        Message<String> receivedMessage = MessageBuilder.withPayload(receivedContent).build();
	        validator.validateMessage(receivedMessage, null, validationContext);
	    } else {
            Assert.isTrue(controlAttachment.getContent() == null || controlAttachment.getContent().trim().length() == 0, 
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + controlAttachment.getContent().trim() + "' but was '"
                        + null + "'");
        }
	}
}
