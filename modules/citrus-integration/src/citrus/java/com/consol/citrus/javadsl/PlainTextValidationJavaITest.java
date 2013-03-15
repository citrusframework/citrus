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

package com.consol.citrus.javadsl;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.message.MessageType;

/**
 * @author Christoph Deppisch
 */
public class PlainTextValidationJavaITest extends TestNGCitrusTestBuilder {
    
    @Override
    public void configure() {
        parallel(
            send("httpMessageSender")
                .payload("Hello, World!"),
            sequential(
                receive("httpRequestReceiver")
                   .messageType(MessageType.PLAINTEXT)
                   .payload("Hello, World!")
                   .extractFromHeader("jms_messageId", "correlation_id"),
                send("httpResponseSender")
                   .payload("Hello, Citrus!")
                   .header("citrus_http_status_code", "200")
                   .header("citrus_http_version", "HTTP/1.1")
                   .header("citrus_http_reason_phrase", "OK")
                   .header("jms_correlationId", "${correlation_id}")
            )
        );
        
        receive("httpResponseReceiver")
            .messageType(MessageType.PLAINTEXT)
            .payload("Hello, Citrus!")
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
        
        send("httpMessageSender")
            .payload("Hello, World!");
        
        sleep(2000);
        
        receive("httpResponseReceiver")
            .messageType(MessageType.PLAINTEXT)
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
    }
    
    @Test
    public void echoActionITest(ITestContext testContext) {
        executeTest(testContext);
    }
}