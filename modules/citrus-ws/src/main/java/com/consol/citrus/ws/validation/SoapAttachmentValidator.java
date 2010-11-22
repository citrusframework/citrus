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

package com.consol.citrus.ws.validation;

import java.io.IOException;

import org.springframework.integration.core.Message;
import org.springframework.ws.mime.Attachment;

import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.WebServiceEndpoint;


/**
 * Interface for SOAP attachment validators. 
 * 
 * The Citrus {@link WebServiceEndpoint} implementation adds the received SOAP attachments as 
 * {@link Attachment} implementations to the Spring integration message header. The header name will be the
 * attachment's contentId. The header value is the {@link Attachment} object.
 *  
 * @author Christoph Deppisch
 */
public interface SoapAttachmentValidator {

    /**
     * Validate the attachment with a given control attachment.
     * 
     * @param receivedMessage
     * @param controlAttachment
     * @throws IOException
     */
    public void validateAttachment(Message<?> receivedMessage, SoapAttachment controlAttachment) throws IOException;
}
