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
 * Custom mail message response used to respond to SMTP command with a specific code and
 * message. By default SMTP command is responded with success OK. User can also specify reject message and code
 * in order to abort SMTP mail communication with error.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "code",
        "message"
})
@XmlRootElement(name = "mail-response")
public class MailResponse {

    /** Default ok code */
    public static final int OK_CODE = 250;
    public static final String OK_MESSAGE = "Ok";

    @XmlElement(required = true, defaultValue = "250")
    protected int code = OK_CODE;
    @XmlElement(required = true, defaultValue = "Ok")
    protected String message = OK_MESSAGE;

    /**
     * Gets the response code.
     * @return
     */
    public int getCode() {
        return code;
    }

    /**
     * Sets the response code.
     * @param code
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * Gets the reject message.
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the reject message.
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
