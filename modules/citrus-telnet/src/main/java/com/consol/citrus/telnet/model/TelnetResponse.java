/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.telnet.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * POJO encapsulate a Telnet response. It is immutable.
 *
 * @author Donat Müller
 * @since 2.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "reply",
})
@XmlRootElement(name = "telnet-response")
public class TelnetResponse implements TelnetMessage {

    @XmlElement(required = true)
    protected String reply;

    /**
     * Default constructor.
     */
    public TelnetResponse() {
    }

    /**
     * Default constructor using fields.
     * @param pReply
     */
    public TelnetResponse(String pReply) {
        reply = pReply;
    }

    /**
     * Gets the reply.
     * @return the reply the reply to get.
     */
    public String getReply() {
        return reply;
    }

}
