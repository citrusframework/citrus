/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.ws.validation;

import java.util.List;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.mime.Attachment;

/**
 * Abstract SOAP attachment validator tries to find attachment within received message and compares
 * its attachment contentId, contentType and content body to a control attachment definition.
 *
 * Validator will create a {@link SoapAttachment} and automatically handle contentId and
 * contentType validation. Content body validation is delegated to subclasses.
 *
 * @author Christoph Deppisch
 */
public abstract class AbstractSoapAttachmentValidator implements SoapAttachmentValidator {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(AbstractSoapAttachmentValidator.class);

    @Override
    public void validateAttachment(SoapMessage soapMessage, List<SoapAttachment> controlAttachments) {
        logger.debug("Validating SOAP attachments ...");

        for (SoapAttachment controlAttachment : controlAttachments) {
            SoapAttachment attachment = findAttachment(soapMessage, controlAttachment);

            if (logger.isDebugEnabled()) {
                logger.debug("Found attachment with contentId '" + controlAttachment.getContentId() + "'");
            }

            validateAttachmentContentId(attachment, controlAttachment);
            validateAttachmentContentType(attachment, controlAttachment);
            validateAttachmentContent(attachment, controlAttachment);

            logger.info("SOAP attachment validation successful: All values OK");
        }
    }

    /**
     * Finds attachment in list of soap attachments on incoming soap message. By default
     * uses content id of control attachment as search key. If no proper attachment with this content id
     * was found in soap message throws validation exception.
     *
     * @param soapMessage
     * @param controlAttachment
     * @return
     */
    protected SoapAttachment findAttachment(SoapMessage soapMessage, SoapAttachment controlAttachment) {
        List<SoapAttachment> attachments = soapMessage.getAttachments();
        Attachment matching = null;

        if (controlAttachment.getContentId() == null) {
            if (attachments.size() == 1) {
                matching = attachments.get(0);
            } else {
                throw new ValidationException("Found more than one SOAP attachment - need control attachment content id for validation!");
            }
        } else {
            // try to find attachment by its content id
            for (Attachment attachment : attachments) {
                if (controlAttachment.getContentId() != null &&
                        controlAttachment.getContentId().equals(attachment.getContentId())) {
                    matching = attachment;
                }
            }
        }

        if (matching != null) {
            return SoapAttachment.from(matching);
        } else {
            throw new ValidationException(String.format("Unable to find SOAP attachment with content id '%s'", controlAttachment.getContentId()));
        }
    }

    /**
     * Validating SOAP attachment content id.
     * @param receivedAttachment
     * @param controlAttachment
     */
    protected void validateAttachmentContentId(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        //in case contentId was not set in test case, skip validation
        if (!StringUtils.hasText(controlAttachment.getContentId())) { return; }

        if (receivedAttachment.getContentId() != null) {
            if (controlAttachment.getContentId() == null) {
                throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentId",
                            null, receivedAttachment.getContentId()));
            }

            if (!receivedAttachment.getContentId().equals(controlAttachment.getContentId())) {
                throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentId",
                            controlAttachment.getContentId(), receivedAttachment.getContentId()));
            }
        } else if (StringUtils.hasText(controlAttachment.getContentId())) {
            throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentId",
                        controlAttachment.getContentId(), null));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating attachment contentId: " + receivedAttachment.getContentId() +
                    "='" + controlAttachment.getContentId() + "': OK.");
        }
    }

    /**
     * Validating SOAP attachment content type.
     * @param receivedAttachment
     * @param controlAttachment
     */
    protected void validateAttachmentContentType(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        //in case contentType was not set in test case, skip validation
        if (!StringUtils.hasText(controlAttachment.getContentType())) { return; }

        if (receivedAttachment.getContentType() != null) {
            if (controlAttachment.getContentType() == null) {
                throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentType",
                            null, receivedAttachment.getContentType()));
            }

            if (!receivedAttachment.getContentType().equals(controlAttachment.getContentType())) {
                throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentType",
                            controlAttachment.getContentType(), receivedAttachment.getContentType()));
            }
        } else if (StringUtils.hasText(controlAttachment.getContentType())) {
            throw new ValidationException(buildValidationErrorMessage("Values not equal for attachment contentType",
                        controlAttachment.getContentType(), null));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating attachment contentType: " + receivedAttachment.getContentType() +
                    "='" + controlAttachment.getContentType() + "': OK.");
        }
    }

    /**
     * Constructs proper error message with expected value and actual value.
     * @param message the base error message.
     * @param expectedValue the expected value.
     * @param actualValue the actual value.
     * @return
     */
    private String buildValidationErrorMessage(String message, Object expectedValue, Object actualValue) {
        return message + ", expected '" + expectedValue + "' but was '" + actualValue + "'";
    }

    /**
     * Delegate content body validation to subclasses.
     * @param receivedAttachment
     * @param controlAttachment
     */
    protected abstract void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment);
}
