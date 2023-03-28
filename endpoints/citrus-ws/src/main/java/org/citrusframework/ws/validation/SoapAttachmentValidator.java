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
import org.citrusframework.ws.message.SoapMessage;

import java.util.List;


/**
 * Interface for SOAP attachment validators. 
 * 
 * The Citrus {@link org.citrusframework.ws.server.WebServiceEndpoint} implementation adds the received SOAP attachments as
 * {@link org.springframework.ws.mime.Attachment} implementations to the Spring integration message header. The header name will be the
 * attachment's contentId. The header value is the {@link org.springframework.ws.mime.Attachment} object.
 *  
 * @author Christoph Deppisch
 */
public interface SoapAttachmentValidator {

    /**
     * Validate attachments in soap message. List of control attachments should
     * be present and get validated.
     * 
     * @param soapMessage
     * @param controlAttachments
     */
    void validateAttachment(SoapMessage soapMessage, List<SoapAttachment> controlAttachments);
}
