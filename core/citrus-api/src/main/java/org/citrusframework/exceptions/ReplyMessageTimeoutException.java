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

public class ReplyMessageTimeoutException extends MessageTimeoutException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     */
    public ReplyMessageTimeoutException(long timeout, String endpoint) {
        super(timeout, endpoint);
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param endpoint
     * @param cause
     */
    public ReplyMessageTimeoutException(long timeout, String endpoint, Throwable cause) {
        super(timeout, endpoint, cause);
    }

    @Override
    public String getDetailMessage() {
        if (timeout <=0 && endpoint == null) {
            return "Failed to receive synchronous reply message.";
        }

        return String.format("Failed to receive synchronous reply message on endpoint: '%s'", endpoint);
    }
}
