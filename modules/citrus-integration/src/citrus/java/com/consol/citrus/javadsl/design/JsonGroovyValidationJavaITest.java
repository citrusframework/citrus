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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.message.MessageType;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class JsonGroovyValidationJavaITest extends TestNGCitrusTestDesigner {
    
    /** OS new line */
    private static final String NEWLINE = System.getProperty("line.separator");
    
    @CitrusTest
    public void jsonGroovyValidation() {
        parallel(
            send("httpClient")
                .payload("{" +
                    "\"type\" : \"read\"," +
                    "\"mbean\" : \"java.lang:type=Memory\"," +
                    "\"attribute\" : \"HeapMemoryUsage\"," +
                    "\"path\" : \"used\"" +
                  "}"),
            sequential(
                receive("httpServerRequestEndpoint")
                   .messageType(MessageType.JSON)
                   .validator("defaultGroovyJsonMessageValidator")
                   .validateScript("assert json.type == 'read'" + NEWLINE +
                              "assert json.mbean == 'java.lang:type=Memory'" + NEWLINE +
                              "assert json.attribute == 'HeapMemoryUsage'")
                   .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                send("httpServerResponseEndpoint")
                   .payload("{" + NEWLINE +
                        "\"timestamp\" : \"2011-01-01\"," + NEWLINE +
                        "\"status\" : 200," + NEWLINE +
                        "\"request\" : " + NEWLINE +
                          "{" + NEWLINE +
                            "\"mbean\" : \"java.lang:type=Memory\"," + NEWLINE +
                            "\"path\" : \"used\"," + NEWLINE +
                            "\"attribute\" : \"HeapMemoryUsage\"," + NEWLINE +
                            "\"type\" : \"read\"" + NEWLINE +
                          "}," + NEWLINE +
                        "\"value\" : 512" + NEWLINE +
                      "}")
                   .header("citrus_http_status_code", "200")
                   .header("citrus_http_version", "HTTP/1.1")
                   .header("citrus_http_reason_phrase", "OK")
                   .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );
        
        receive("httpClient")
            .messageType(MessageType.JSON)
            .validator("defaultGroovyJsonMessageValidator")
            .validateScript("assert json.request.type == 'read'" + NEWLINE +
                              "assert json.request.mbean == 'java.lang:type=Memory'" + NEWLINE +
                              "assert json.request.attribute == 'HeapMemoryUsage'" + NEWLINE +
                              "assert json.status == 200" + NEWLINE +
                              "assert json.value >= 256" + NEWLINE +
                              "assert json.value <= 1024")
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
    }
}