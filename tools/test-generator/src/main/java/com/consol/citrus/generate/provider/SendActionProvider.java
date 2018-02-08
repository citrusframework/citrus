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

package com.consol.citrus.generate.provider;

import com.consol.citrus.message.Message;
import com.consol.citrus.model.testcase.core.SendModel;
import org.springframework.util.CollectionUtils;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class SendActionProvider implements MessageActionProvider<SendModel, Message> {

    @Override
    public SendModel getAction(String endpoint, Message message) {
        SendModel send = new SendModel();
        send.setEndpoint(endpoint);
        SendModel.Message sendMessage = new SendModel.Message();
        sendMessage.setData(message.getPayload(String.class));
        send.setMessage(sendMessage);

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            SendModel.Header header = new SendModel.Header();

            message.getHeaders().forEach((key, value) -> {
                SendModel.Header.Element element = new SendModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());

                if (!value.getClass().equals(String.class)) {
                    element.setType(value.getClass().getSimpleName().toLowerCase());
                }

                header.getElements().add(element);
            });

            send.setHeader(header);
        }

        return send;
    }
}
