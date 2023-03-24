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
 * Accept request is raised for each mail communication in prior to processing the mail message. If
 * accepted with positive accept outcome mail message is processed. If not the mail message is declined with
 * not accepted error.
 *
 * Accept request is defined by mail sender address and recipients. Accept request is fired for each entry in list
 * of recipients.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "from",
        "to"
})
@XmlRootElement(name = "accept-request")
public class AcceptRequest {

    @XmlElement(required = true)
    protected String from;
    @XmlElement(required = true)
    protected String to;

    /**
     * Default constructor.
     */
    public AcceptRequest() {
    }

    /**
     * Constructor using fields.
     * @param from
     * @param to
     */
    public AcceptRequest(String from, String to) {
        this.from = from;
        this.to = to;
    }

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
}
