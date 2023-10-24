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
 * Mail message model object representing mail content with sender and recipient information. Body can be text, binary
 * or multipart nature.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "from",
        "to",
        "cc",
        "bcc",
        "subject",
        "replyTo",
        "body"
})
@XmlRootElement(name = "mail-message")
public class MailRequest {

    @XmlElement(required = true)
    protected String from;
    @XmlElement(required = true)
    protected String to;
    @XmlElement
    protected String cc;
    @XmlElement
    protected String bcc;
    @XmlElement(required = true)
    protected String subject;
    @XmlElement(required = false)
    protected String replyTo;
    @XmlElement(required = true)
    protected BodyPart body;

    /**
     * Gets the sender mail address.
     * @return
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets the sender mail address.
     * @param from
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Gets the mail recipients.
     * @return
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the mail recipients.
     * @param to
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Gets the copy to recipients.
     * @return
     */
    public String getCc() {
        return cc;
    }

    /**
     * Sets the copy to recipients.
     * @param cc
     */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /**
     * Gets the blind copy recipient list.
     * @return
     */
    public String getBcc() {
        return bcc;
    }

    /**
     * Sets the blind copy recipient list.
     * @param bcc
     */
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    /**
     * Gets the reply to mail address.
     * @return
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the reply to mail address.
     * @param replyTo
     */
    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    /**
     * Gets the mail subject.
     * @return
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the mail subject.
     * @param subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the mail body content.
     * @return
     */
    public BodyPart getBody() {
        return body;
    }

    /**
     * Sets the mail body content.
     * @param body
     */
    public void setBody(BodyPart body) {
        this.body = body;
    }

}
