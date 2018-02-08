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

package com.consol.citrus.generate.provider.http;

import com.consol.citrus.generate.provider.MessageActionProvider;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.model.testcase.http.*;

import java.util.Optional;

/**
 * @author Christoph Deppisch
 * @since 2.7.4
 */
public class ReceiveHttpResponseActionProvider implements MessageActionProvider<ReceiveResponseModel, HttpMessage> {

    @Override
    public ReceiveResponseModel getAction(String endpoint, HttpMessage message) {
        ReceiveResponseModel response = new ReceiveResponseModel();

        response.setClient(endpoint);

        ReceiveResponseModel.Body body = new ReceiveResponseModel.Body();
        body.setData(message.getPayload(String.class));
        response.setBody(body);

        ReceiveResponseModel.Headers responseHeaders = new ReceiveResponseModel.Headers();
        responseHeaders.setStatus(message.getStatusCode().toString());
        responseHeaders.setReasonPhrase(message.getStatusCode().getReasonPhrase());

        message.getHeaders().entrySet().stream()
                .filter(entry -> !entry.getKey().startsWith(MessageHeaders.PREFIX))
                .forEach(entry -> {
                    ResponseHeadersType.Header header = new ResponseHeadersType.Header();
                    header.setName(entry.getKey());
                    header.setValue(Optional.ofNullable(entry.getValue()).map(Object::toString).orElse(""));
                    responseHeaders.getHeaders().add(header);
                });

        response.setHeaders(responseHeaders);

        return response;
    }
}
