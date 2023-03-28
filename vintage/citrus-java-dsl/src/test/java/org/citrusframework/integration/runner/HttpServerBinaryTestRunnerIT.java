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

package org.citrusframework.integration.runner;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.testng.TestNGCitrusTestRunner;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.MessageType;
import org.apache.hc.core5.http.ContentType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpServerBinaryTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void httpServerBinary() {
        byte[] binaryData = "$&%!!".getBytes();

        http(action -> action.client("echoHttpClient")
                .send()
                .post()
                .fork(true)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        http(action -> action.server("echoHttpServer")
                    .receive()
                    .post("/test")
                    .messageType(MessageType.BINARY)
                    .message(new DefaultMessage(binaryData))
                    .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType())
                    .accept(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        http(action -> action.server("echoHttpServer")
                .send()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));

        http(action -> action.client("echoHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.BINARY)
                .message(new DefaultMessage(binaryData))
                .contentType(ContentType.APPLICATION_OCTET_STREAM.getMimeType()));
    }
}
