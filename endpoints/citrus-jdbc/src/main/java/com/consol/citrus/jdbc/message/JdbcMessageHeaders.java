/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.jdbc.message;

import com.consol.citrus.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public abstract class JdbcMessageHeaders {

    /**
     * Prevent instantiation.
     */
    private JdbcMessageHeaders() {
    }

    /** Special header prefix for jdbc transport headers */
    public static final String JDBC_PREFIX = MessageHeaders.PREFIX + "jdbc_";
    public static final String JDBC_SERVER_PREFIX = JDBC_PREFIX + "server_";

    public static final String JDBC_ROWS_UPDATED = JDBC_PREFIX + "rows_updated";

    public static final String JDBC_SERVER_SUCCESS = JDBC_SERVER_PREFIX + "success";
    public static final String JDBC_SERVER_EXCEPTION = JDBC_SERVER_PREFIX + "exception";
}
