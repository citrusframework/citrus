/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.jmx.message;

import com.consol.citrus.jmx.endpoint.JmxEndpointConfiguration;
import com.consol.citrus.message.*;

import javax.management.Notification;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class JmxMessageConverter implements MessageConverter<Notification, JmxEndpointConfiguration> {

    @Override
    public Notification convertOutbound(Message internalMessage, JmxEndpointConfiguration endpointConfiguration) {
        return null;
    }

    @Override
    public void convertOutbound(Notification externalMessage, Message internalMessage, JmxEndpointConfiguration endpointConfiguration) {
    }

    @Override
    public Message convertInbound(Notification externalMessage, JmxEndpointConfiguration endpointConfiguration) {
        return new DefaultMessage(externalMessage.getMessage());
    }
}
