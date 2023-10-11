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

package org.citrusframework.generate.provider;

import java.util.Optional;

import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.model.testcase.core.SendModel;

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

        if (message.getHeaders() != null && !message.getHeaders().isEmpty()) {
            SendModel.Header header = new SendModel.Header();

            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> {
                        SendModel.Header.Element element = new SendModel.Header.Element();
                        element.setName(entry.getKey());
                        element.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));

                        if (!element.getValue().getClass().equals(String.class)) {
                            element.setType(element.getValue().getClass().getSimpleName().toLowerCase());
                        }

                        header.getElements().add(element);
                    });

            send.setHeader(header);
        }

        return send;
    }
}
