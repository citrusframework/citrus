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

package org.citrusframework.mail.model;

import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.citrusframework.mail.model
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BodyPart.Attachments }
     */
    public BodyPart.Attachments createBodyPartAttachments() {
        return new BodyPart.Attachments();
    }

    /**
     * Create an instance of {@link BodyPart }
     */
    public BodyPart createBodyPart() {
        return new BodyPart();
    }

    /**
     * Create an instance of {@link MailResponse }
     */
    public MailResponse createMailResponse() {
        return new MailResponse();
    }

    /**
     * Create an instance of {@link AcceptResponse }
     */
    public AcceptResponse createAcceptResponse() {
        return new AcceptResponse();
    }

    /**
     * Create an instance of {@link MailRequest }
     */
    public MailRequest createMailMessage() {
        return new MailRequest();
    }

    /**
     * Create an instance of {@link AttachmentPart }
     */
    public AttachmentPart createAttachmentPart() {
        return new AttachmentPart();
    }

    /**
     * Create an instance of {@link AcceptRequest }
     */
    public AcceptRequest createAcceptRequest() {
        return new AcceptRequest();
    }

}
