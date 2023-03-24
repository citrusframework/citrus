/*
 * Copyright 2006-2010 the original author or authors.
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

/**
 * Throw this exception in case you did not receive a message on a destination in time.
 * Used in message receivers to state that expected message did not arrive.
 *
 * @author Christoph Deppisch
 */
public class ActionTimeoutException extends CitrusRuntimeException {

    private static final long serialVersionUID = -8652778602073652873L;

    protected final long timeout;

    /**
     * Default constructor
     */
    public ActionTimeoutException() {
        this(0L);
    }

    /**
     * Constructor using fields.
     * @param timeout
     */
    public ActionTimeoutException(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Constructor using fields.
     * @param timeout
     * @param cause
     */
    public ActionTimeoutException(long timeout, Throwable cause) {
        super(cause);
        this.timeout = timeout;
    }

    @Override
    public String getMessage() {
        if (timeout <= 0) {
            return String.format("Action timeout. %s", getDetailMessage()).trim();
        }

        return String.format("Action timeout after %s milliseconds. %s", timeout, getDetailMessage()).trim();
    }

    protected String getDetailMessage() {
        return "";
    }
}
