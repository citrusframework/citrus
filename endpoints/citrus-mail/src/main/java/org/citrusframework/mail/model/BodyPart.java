/*
 * Copyright 2006-2013 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Body part representation holds content as String and optional attachment parts.
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BodyPart", propOrder = {
        "contentType",
        "content",
        "attachments"
})
@XmlSeeAlso({
        AttachmentPart.class
})
public class BodyPart {

    @XmlElement(required = true)
    protected String contentType;
    @XmlElement(required = true)
    protected String content;
    protected BodyPart.Attachments attachments;

    /**
     * Default constructor.
     */
    public BodyPart() {
    }

    /**
     * Default constructor using content and contentType.
     * @param content
     * @param contentType
     */
    public BodyPart(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    /**
     * Adds new attachment part.
     * @param part
     */
    public void addPart(AttachmentPart part) {
        if (attachments == null) {
            attachments = new BodyPart.Attachments();
        }

        this.attachments.add(part);
    }

    /**
     * Gets the content type.
     * @return
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the content as string.
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content as string.
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the attachment list.
     * @return
     */
    public Attachments getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachment list.
     * @param attachments
     */
    public void setAttachments(Attachments attachments) {
        this.attachments = attachments;
    }

    /**
     * Checks if attachments are present.
     * @return
     */
    public boolean hasAttachments() {
        return attachments != null && attachments.getAttachments() != null && !attachments.getAttachments().isEmpty();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "attachments"
    })
    public static class Attachments {
        @XmlElement(name = "attachment", required = true)
        protected List<AttachmentPart> attachments = new ArrayList<>();

        public List<AttachmentPart> getAttachments() {
            return this.attachments;
        }

        public void add(AttachmentPart attachmentPart) {
            this.attachments.add(attachmentPart);
        }
    }
}
