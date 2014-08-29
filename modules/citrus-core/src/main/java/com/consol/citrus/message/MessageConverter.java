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

package com.consol.citrus.message;

import org.springframework.integration.Message;

/**
 * Message converter interface describes methods for converting a external message type T to the internal message
 * representation and vice versa.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface MessageConverter<T> {

    /**
     * Converts internal message representation to external message for outbound communication.
     *
     * @param internalMessage
     * @return
     */
    T convertOutbound(Message<?> internalMessage);

    /**
     * Converts internal message representation to external message for outbound communication.
     * Method receives prepared external message object as parameter argument which is then enriched with information
     * from internal message.
     *
     * @param externalMessage
     * @param internalMessage
     * @return
     */
    void convertOutbound(T externalMessage, Message<?> internalMessage);

    /**
     * Converts external message to internal representation.
     *
     * @param externalMessage
     * @return
     */
    Message<?> convertInbound(T externalMessage);
}
