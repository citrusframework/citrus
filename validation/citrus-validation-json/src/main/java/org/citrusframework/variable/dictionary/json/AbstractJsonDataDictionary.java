/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary.json;

import org.citrusframework.message.MessageType;
import org.citrusframework.variable.dictionary.AbstractDataDictionary;

/**
 * Abstract json data dictionary works on json message data. Each value is translated with dictionary.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public abstract class AbstractJsonDataDictionary extends AbstractDataDictionary<String> {

    /**
     * Checks if this message interceptor is capable of this message type. XML message interceptors may only apply to this message
     * type while JSON message interceptor implementations do not and vice versa.
     *
     * @param messageType the message type representation as String (e.g. xml, json, csv, plaintext).
     * @return true if this message interceptor supports the message type.
     */
    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.JSON.toString().equalsIgnoreCase(messageType);
    }
}
