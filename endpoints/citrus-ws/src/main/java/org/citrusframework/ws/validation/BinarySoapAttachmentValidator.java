/*
 * Copyright 2006-2015 the original author or authors.
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

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.ws.message.SoapAttachment;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Optional;

/**
 * Soap attachment validator performs binary content validation by comparing attachment content binary input streams.
 *
 * @author Christoph Deppisch
 * @since 2.1
 */
public class BinarySoapAttachmentValidator extends AbstractSoapAttachmentValidator {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(BinarySoapAttachmentValidator.class);

    @Override
    protected void validateAttachmentContent(SoapAttachment receivedAttachment, SoapAttachment controlAttachment) {
        if (log.isDebugEnabled()) {
            log.debug("Validating binary SOAP attachment content ...");
        }

        try {
            Assert.isTrue(IOUtils.contentEquals(receivedAttachment.getInputStream(), controlAttachment.getInputStream()),
                    "Values not equal for binary attachment content '"
                            + Optional.ofNullable(controlAttachment.getContentId()).orElse(Optional.ofNullable(receivedAttachment.getContentId()).orElse("unknown")) + "'");
        } catch(IOException e) {
            throw new CitrusRuntimeException("Binary SOAP attachment validation failed", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Validating binary SOAP attachment content: OK");
        }
    }
}
