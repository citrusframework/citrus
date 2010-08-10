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

package com.consol.citrus.message;

import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageHeaders;

/**
 * Default message correlator implementation using the Spring integration message id
 * as correlation key.
 * 
 * @author Christoph Deppisch
 */
public class DefaultReplyMessageCorrelator implements ReplyMessageCorrelator {

    /**
     * @see com.consol.citrus.message.ReplyMessageCorrelator#getCorrelationKey(org.springframework.integration.core.Message)
     */
    public String getCorrelationKey(Message<?> request) {
        return MessageHeaders.ID + " = '" + request.getHeaders().getId().toString() + "'";
    }

    /**
     * @see com.consol.citrus.message.ReplyMessageCorrelator#getCorrelationKey(java.lang.String)
     */
    public String getCorrelationKey(String id) {
        return MessageHeaders.ID + " = '" + id + "'";
    }
}
