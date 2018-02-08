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
import com.consol.citrus.model.testcase.core.ReceiveModel;
import org.springframework.util.CollectionUtils;

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

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            ReceiveModel.Header header = new ReceiveModel.Header();

            message.getHeaders().forEach((key, value) -> {
                ReceiveModel.Header.Element element = new ReceiveModel.Header.Element();
                element.setName(key);
                element.setValue(value.toString());
                header.getElements().add(element);
            });

            receive.setHeader(header);
        }

        return receive;
    }
}
