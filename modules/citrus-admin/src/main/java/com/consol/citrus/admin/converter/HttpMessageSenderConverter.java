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

package com.consol.citrus.admin.converter;

import com.consol.citrus.admin.model.MessageSenderItem;
import com.consol.citrus.model.config.http.MessageSender;

/**
 * @author Christoph Deppisch
 * @since 1.3.1
 */
public class HttpMessageSenderConverter implements MessageSenderConverter<MessageSender> {

    @Override
    public MessageSenderItem convert(MessageSender httpMessageSender) {
        MessageSenderItem messageSenderType = new MessageSenderItem();

        messageSenderType.setName(httpMessageSender.getId());
        messageSenderType.setDestination(httpMessageSender.getRequestUrl());
        messageSenderType.setType("HTTP");

        return messageSenderType;
    }
}
