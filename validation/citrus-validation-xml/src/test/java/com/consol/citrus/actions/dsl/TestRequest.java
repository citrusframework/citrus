/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.actions.dsl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Sample model object marshalled to message payload.
 *
 * @author Christoph Deppisch
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "message"
})
@XmlRootElement(name = "TestRequest")
public class TestRequest {

    @XmlElement(name="Message", required = true)
    private String message;

    /**
     * Default constructor.
     */
    public TestRequest() {
    }

    /**
     * Default constructor using message field.
     * @param message
     */
    public TestRequest(String message) {
        this.message = message;
    }

    /**
     * Gets the message.
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message.
     * @param message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
