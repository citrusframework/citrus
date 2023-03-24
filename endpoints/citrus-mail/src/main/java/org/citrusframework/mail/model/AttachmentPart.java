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

import jakarta.xml.bind.annotation.*;

/**
 * Attachment part adds file name.
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AttachmentPartType", propOrder = {
        "fileName"
})
public class AttachmentPart extends BodyPart {

    @XmlElement(required = true)
    protected String fileName;

    /**
     * Default constructor.
     */
    public AttachmentPart() {
    }

    /**
     * Default constructor using fields.
     * @param content
     * @param contentType
     */
    public AttachmentPart(String content, String contentType, String fileName) {
        super(content, contentType);
        this.fileName = fileName;
    }

    /**
     * Gets the file name.
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the file name.
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
