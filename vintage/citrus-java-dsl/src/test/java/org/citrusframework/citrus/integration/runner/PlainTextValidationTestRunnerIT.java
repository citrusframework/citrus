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

package org.citrusframework.citrus.integration.runner;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.dsl.testng.TestNGCitrusTestRunner;
import org.citrusframework.citrus.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class PlainTextValidationTestRunnerIT extends TestNGCitrusTestRunner {

    @CitrusTest
    public void plainTextValidation() {
        parallel().actions(
            http(builder -> builder.client("httpClient")
                    .send()
                    .post()
                    .payload("Hello, World!")),
            sequential().actions(
                http(builder -> builder.server("httpServerRequestEndpoint")
                        .receive()
                        .post()
                        .messageType(MessageType.PLAINTEXT)
                        .payload("Hello, World!")
                        .extractFromHeader("citrus_jms_messageId", "correlation_id")),
                http(builder -> builder.server("httpServerResponseEndpoint")
                        .send()
                        .response(HttpStatus.OK)
                        .payload("Hello, Citrus!")
                        .version("HTTP/1.1")
                        .header("citrus_jms_correlationId", "${correlation_id}"))
            )
        );

        http(builder -> builder.client("httpClient")
                .receive()
                .response(HttpStatus.OK)
                .messageType(MessageType.PLAINTEXT)
                .payload("Hello, Citrus!")
                .version("HTTP/1.1"));

        http(builder -> builder.client("httpClient")
                .send()
                .post()
                .payload("Hello, World!"));

        sleep(2000);

        http(builder -> builder.client("httpClient")
                .receive()
                .response()
                .messageType(MessageType.PLAINTEXT)
                .version("HTTP/1.1"));
    }
}
