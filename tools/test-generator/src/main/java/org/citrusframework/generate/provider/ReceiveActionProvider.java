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
import org.citrusframework.model.testcase.core.ReceiveModel;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReceiveActionProvider implements MessageActionProvider<ReceiveModel, Message> {

    @Override
    public ReceiveModel getAction(String endpoint, Message message) {
        ReceiveModel receive = new ReceiveModel();

        receive.setEndpoint(endpoint);

        ReceiveModel.Message receiveMessage = new ReceiveModel.Message();
        receiveMessage.setData(message.getPayload(String.class));
        receive.setMessage(receiveMessage);

        if (message.getHeaders() != null && !message.getHeaders().isEmpty()) {
            ReceiveModel.Header header = new ReceiveModel.Header();

            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> {
                        ReceiveModel.Header.Element element = new ReceiveModel.Header.Element();
                        element.setName(entry.getKey());
                        element.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));
                        header.getElements().add(element);
                    });

            receive.setHeader(header);
        }

        return receive;
    }
}
