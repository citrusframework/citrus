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

package org.citrusframework.endpoint.direct;

import org.citrusframework.message.MessageHeaders;

/**
 * @author Christoph Deppisch
 */
public class DirectMessageHeaders {

    /** Message reply queue name header */
    public static final String REPLY_QUEUE = MessageHeaders.PREFIX + "reply_queue";

    /**
     * Prevent instantiation of utility class.
     */
    private DirectMessageHeaders() {
        // utility class
    }
}