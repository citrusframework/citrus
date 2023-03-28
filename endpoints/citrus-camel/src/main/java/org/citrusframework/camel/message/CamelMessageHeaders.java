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

package org.citrusframework.camel.message;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public abstract class CamelMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private CamelMessageHeaders() {
    }

    /** Special header prefix for camel transport headers */
    public static final String CAMEL_PREFIX = MessageHeaders.PREFIX + "camel_";

    public static final String EXCHANGE_ID = CAMEL_PREFIX + "exchange_id";
    public static final String EXCHANGE_PATTERN = CAMEL_PREFIX + "exchange_pattern";
    public static final String EXCHANGE_FAILED = CAMEL_PREFIX + "exchange_failed";
    public static final String EXCHANGE_EXCEPTION = CAMEL_PREFIX + "exchange_exception";
    public static final String EXCHANGE_EXCEPTION_MESSAGE = CAMEL_PREFIX + "exchange_exception_message";
    public static final String ROUTE_ID = CAMEL_PREFIX + "route_id";

}
