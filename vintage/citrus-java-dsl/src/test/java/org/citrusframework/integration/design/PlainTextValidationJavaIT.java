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

package org.citrusframework.integration.design;

import org.citrusframework.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.message.MessageType;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class PlainTextValidationJavaIT extends TestNGCitrusTestDesigner {

    @CitrusTest
    public void plainTextValidation() {
        parallel().actions(
            http().client("httpClient")
                .send()
                .post()
                .payload("Hello, World!"),
            sequential().actions(
                http().server("httpServerRequestEndpoint")
                   .receive()
                   .post()
                   .messageType(MessageType.PLAINTEXT)
                   .payload("Hello, World!")
                   .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                http().server("httpServerResponseEndpoint")
                   .send()
                   .response(HttpStatus.OK)
                   .payload("Hello, Citrus!")
                   .version("HTTP/1.1")
                   .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello, Citrus!")
            .version("HTTP/1.1");

        http().client("httpClient")
            .send()
            .post()
            .payload("Hello, World!");

        sleep(2000);

        http().client("httpClient")
            .receive()
            .response(HttpStatus.OK)
            .messageType(MessageType.PLAINTEXT)
            .version("HTTP/1.1");
    }
}
