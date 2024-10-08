/*
 * Copyright the original author or authors.
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

package org.citrusframework.actions.dsl;

/**
 * Sample model object mapped to message payload.
 *
 */
public class TestRequest {

    /** This requests message */
    private String message;

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
