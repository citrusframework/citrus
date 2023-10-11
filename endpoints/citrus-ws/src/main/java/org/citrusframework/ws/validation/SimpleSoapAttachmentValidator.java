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

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.StringUtils;
import org.citrusframework.ws.message.SoapAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of a {@link AbstractSoapAttachmentValidator}.
 * Attachment content body is validated through simple string equals assertion.
 *
 * @author Christoph Deppisch
 */
public class SimpleSoapAttachmentValidator extends AbstractSoapAttachmentValidator {

    /** Ignores all whitespaces in attachment content */
    private boolean ignoreAllWhitespaces = false;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SimpleSoapAttachmentValidator.class);

    @Override
    protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        String receivedContent = receivedAttachment.getContent().replaceAll("\\s", "");
        String controlContent = controlAttachment.getContent().replaceAll("\\s", "");

        if (logger.isDebugEnabled()) {
            logger.debug("Validating SOAP attachment content ...");
            logger.debug("Received attachment content: " + receivedContent);
            logger.debug("Control attachment content: " + controlContent);
        }

        if (StringUtils.hasText(receivedContent)) {
            if (!StringUtils.hasText(controlContent)) {
                throw new ValidationException("Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + controlContent + "' but was '"
                        + receivedContent + "'");
            }

            validateAttachmentContentData(receivedContent, controlContent, controlAttachment.getContentId());
        } else if (StringUtils.hasText(controlContent)) {
            throw new ValidationException("Values not equal for attachment content '"
                    + controlAttachment.getContentId() + "', expected '"
                    + controlContent + "' but was '"
                    + receivedContent + "'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Validating attachment content: OK");
        }
    }

    /**
     * Validates content data.
     * @param receivedContent
     * @param controlContent
     * @param controlContentId
     */
    protected void validateAttachmentContentData(String receivedContent, String controlContent, String controlContentId) {
        if (ignoreAllWhitespaces) {
            controlContent = controlContent.replaceAll("\\s", "");
            receivedContent = receivedContent.replaceAll("\\s", "");
        }

        if (!receivedContent.equals(controlContent)) {
            throw new ValidationException("Values not equal for attachment content '"
                        + controlContentId + "', expected '"
                        + controlContent.trim() + "' but was '"
                        + receivedContent.trim() + "'");
        }
    }

    /**
     * Gets flag marking that all whitespaces are ignored.
     * @return
     */
    public boolean isIgnoreAllWhitespaces() {
        return ignoreAllWhitespaces;
    }

    /**
     * Sets ignore all whitespaces flag.
     * @param ignoreAllWhitespaces
     */
    public void setIgnoreAllWhitespaces(boolean ignoreAllWhitespaces) {
        this.ignoreAllWhitespaces = ignoreAllWhitespaces;
    }
}
