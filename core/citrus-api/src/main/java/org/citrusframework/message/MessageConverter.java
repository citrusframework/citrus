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

package org.citrusframework.message;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointConfiguration;

/**
 * Message converter interface describes methods for converting a external message type T to the internal message
 * representation and vice versa.
 *
 * @author Christoph Deppisch
 * @since 2.0
 */
public interface MessageConverter<I, O, C extends EndpointConfiguration> {

    /**
     * Converts internal message representation to external message for outbound communication.
     *
     * @param internalMessage
     * @param endpointConfiguration
     * @param context
     * @return
     */
    O convertOutbound(Message internalMessage, C endpointConfiguration, TestContext context);

    /**
     * Converts internal message representation to external message for outbound communication.
     * Method receives prepared external message object as parameter argument which is then enriched with information
     * from internal message.
     *
     * @param externalMessage
     * @param internalMessage
     * @param endpointConfiguration
     * @param context
     * @return
     */
    void convertOutbound(O externalMessage, Message internalMessage, C endpointConfiguration, TestContext context);

    /**
     * Converts external message to internal representation.
     *
     * @param externalMessage
     * @param endpointConfiguration
     * @param context
     * @return
     */
    Message convertInbound(I externalMessage, C endpointConfiguration, TestContext context);
}
