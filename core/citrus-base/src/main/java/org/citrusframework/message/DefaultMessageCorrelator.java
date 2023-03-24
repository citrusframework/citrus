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

package org.citrusframework.message;

/**
 * Default message correlator implementation using the Citrus message id
 * as correlation key.
 *
 * @author Christoph Deppisch
 */
public class DefaultMessageCorrelator implements MessageCorrelator {

    @Override
    public String getCorrelationKey(Message request) {
        return MessageHeaders.ID + " = '" + request.getId() + "'";
    }

    @Override
    public String getCorrelationKey(String id) {
        return MessageHeaders.ID + " = '" + id + "'";
    }

    @Override
    public String getCorrelationKeyName(String consumerName) {
        return MessageHeaders.MESSAGE_CORRELATION_KEY + "_" + consumerName;
    }
}
