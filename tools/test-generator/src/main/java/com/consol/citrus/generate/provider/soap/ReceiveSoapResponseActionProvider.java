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

package com.consol.citrus.generate.provider.soap;

import com.consol.citrus.generate.provider.MessageActionProvider;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.model.testcase.ws.ReceiveModel;
import com.consol.citrus.ws.message.SoapMessage;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReceiveSoapResponseActionProvider implements MessageActionProvider<ReceiveModel, SoapMessage> {

    @Override
    public ReceiveModel getAction(String endpoint, SoapMessage message) {
        ReceiveModel response = new ReceiveModel();

        response.setEndpoint(endpoint);

        com.consol.citrus.model.testcase.core.ReceiveModel.Message receiveMessage = new com.consol.citrus.model.testcase.core.ReceiveModel.Message();
        receiveMessage.setData(message.getPayload(String.class));
        response.setMessage(receiveMessage);

        if (!CollectionUtils.isEmpty(message.getHeaders())) {
            com.consol.citrus.model.testcase.core.ReceiveModel.Header header = new com.consol.citrus.model.testcase.core.ReceiveModel.Header();

            message.getHeaders().entrySet().stream()
                    .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                    .forEach(entry -> {
                        com.consol.citrus.model.testcase.core.ReceiveModel.Header.Element element = new com.consol.citrus.model.testcase.core.ReceiveModel.Header.Element();
                        element.setName(entry.getKey());
                        element.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));
                        header.getElements().add(element);
                    });

            response.setHeader(header);
        }

        return response;
    }
}
