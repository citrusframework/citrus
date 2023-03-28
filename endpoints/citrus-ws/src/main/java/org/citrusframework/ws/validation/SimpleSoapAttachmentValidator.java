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

import org.citrusframework.ws.message.SoapAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
    private static Logger log = LoggerFactory.getLogger(SimpleSoapAttachmentValidator.class);

    @Override
    protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        String receivedContent = StringUtils.trimWhitespace(receivedAttachment.getContent());
        String controlContent = StringUtils.trimWhitespace(controlAttachment.getContent());

        if (log.isDebugEnabled()) {
            log.debug("Validating SOAP attachment content ...");
            log.debug("Received attachment content: " + receivedContent);
            log.debug("Control attachment content: " + controlContent);
        }

        if (receivedContent != null) {
            Assert.isTrue(controlContent != null,
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + null + "' but was '"
                        + receivedContent + "'");

            validateAttachmentContentData(receivedContent, controlContent, controlAttachment.getContentId());
        } else {
            Assert.isTrue(!StringUtils.hasLength(controlContent),
                    "Values not equal for attachment content '"
                        + controlAttachment.getContentId() + "', expected '"
                        + controlContent + "' but was '"
                        + null + "'");
        }

        if (log.isDebugEnabled()) {
            log.debug("Validating attachment content: OK");
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
            controlContent = StringUtils.trimAllWhitespace(controlContent);
            receivedContent = StringUtils.trimAllWhitespace(receivedContent);
        }

        Assert.isTrue(receivedContent.equals(controlContent),
                "Values not equal for attachment content '"
                        + controlContentId + "', expected '"
                        + controlContent.trim() + "' but was '"
                        + receivedContent.trim() + "'");
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
