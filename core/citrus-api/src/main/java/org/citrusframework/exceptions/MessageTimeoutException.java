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

package org.citrusframework.exceptions;

public class MessageTimeoutException extends ActionTimeoutException {

    protected final String endpoint;

    /**
     * Default constructor
     */
    public MessageTimeoutException() {
        this(0L, "");
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     */
    public MessageTimeoutException(long timeout, String endpoint) {
        super(timeout);
        this.endpoint = endpoint;
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     * @param cause
     */
    public MessageTimeoutException(long timeout, String endpoint, Throwable cause) {
        super(timeout, cause);
        this.endpoint = endpoint;
    }

    @Override
    public String getDetailMessage() {
        if (timeout <=0 && endpoint == null) {
            return "Failed to receive message.";
        }

        return String.format("Failed to receive message on endpoint: '%s'", endpoint);
    }
}
