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
public class JsonTextValidationJavaITest extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void jsonTextValidation() {
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
                   .payload("{" +
                            "\"type\" : \"read\"," +
                            "\"mbean\" : \"java.lang:type=Memory\"," +
                            "\"attribute\" : \"HeapMemoryUsage\"," +
                            "\"path\" : \"@equalsIgnoreCase('USED')@\"" +
                          "}")
                   .extractFromHeader("citrus_jms_messageId", "correlation_id"),
                send("httpServerResponseEndpoint")
                   .payload("{" +
                        "\"timestamp\" : \"2011-01-01\"," +
                        "\"status\" : 200," +
                        "\"request\" : " +
                          "{" +
                            "\"mbean\" : \"java.lang:type=Memory\"," +
                            "\"path\" : \"used\"," +
                            "\"attribute\" : \"HeapMemoryUsage\"," +
                            "\"type\" : \"read\"" +
                          "}," +
                        "\"value\" : 512" +
                      "}")
                   .header("citrus_http_status_code", "200")
                   .header("citrus_http_version", "HTTP/1.1")
                   .header("citrus_http_reason_phrase", "OK")
                   .header("citrus_jms_correlationId", "${correlation_id}")
            )
        );
        
        receive("httpClient")
            .messageType(MessageType.JSON)
            .payload("{" +
                        "\"timestamp\" : \"@matchesDatePattern('yyyy-MM-dd')@\"," +
                        "\"status\" : 200," +
                        "\"request\" : " +
                          "{" +
                            "\"mbean\" : \"java.lang:type=Memory\"," +
                            "\"path\" : \"@matches('u*s*e*d*')@\"," +
                            "\"attribute\" : \"HeapMemoryUsage\"," +
                            "\"type\" : \"read\"" +
                          "}," +
                        "\"value\" : \"@isNumber()@\"" +
                      "}")
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
        
        send("httpClient")
            .payload("{" +
                "\"type\" : \"read\"," +
                "\"mbean\" : \"java.lang:type=Memory\"," +
                "\"attribute\" : \"HeapMemoryUsage\"," +
                "\"path\" : \"used\"" +
              "}");
        
        sleep(2000);
        
        receive("httpClient")
            .messageType(MessageType.JSON)
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
    }
}