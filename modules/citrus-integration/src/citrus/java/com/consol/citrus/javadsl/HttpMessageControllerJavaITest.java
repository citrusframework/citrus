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

import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.dsl.annotations.CitrusTest;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpMessageControllerJavaITest extends TestNGCitrusTestBuilder {
    
    @CitrusTest(name = "HttpMessageControllerJavaITest")
    public void httpMessageControllerITest() {
        variable("id", "123456789");
        
        echo("First request without query parameter and context path variables.");
        
        parallel(
            send("httpMessageSender")
                .header("citrus_endpoint_uri", "http://localhost:8072")
                .header("citrus_http_method", "GET")
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                receive("httpRequestReceiver")
                    .header("Host", "localhost:8072")
                    .header("Content-Type", "text/html;charset=ISO-8859-1")
                    .header("Accept", "application/xml;charset=UTF-8")
                    .header("citrus_http_method", "GET")
                    .header("citrus_http_request_uri", "/")
                    .header("citrus_http_context_path", "")
                    .header("citrus_http_query_params", "")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
        
        
        echo("Use context path variables.");
        
        parallel(
            send("httpMessageSender")
                .header("citrus_endpoint_uri", "http://localhost:8072/test/user/${id}")
                .header("citrus_http_method", "GET")
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                receive("httpRequestReceiver")
                    .header("Host", "localhost:8072")
                    .header("Content-Type", "text/html;charset=ISO-8859-1")
                    .header("Accept", "application/xml;charset=UTF-8")
                    .header("citrus_http_method", "GET")
                    .header("citrus_http_request_uri", "/test/user/${id}")
                    .header("citrus_http_context_path", "")
                    .header("citrus_http_query_params", "")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
        
        echo("Use query parameter and context path variables.");
        
        parallel(
            send("httpMessageSender")
                .header("citrus_endpoint_uri", "http://localhost:8072/test/user?id=${id}&name=TestUser")
                .header("citrus_http_method", "GET")
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                receive("httpRequestReceiver")
                    .header("Host", "localhost:8072")
                    .header("Content-Type", "text/html;charset=ISO-8859-1")
                    .header("Accept", "application/xml;charset=UTF-8")
                    .header("citrus_http_method", "GET")
                    .header("citrus_http_request_uri", "/test/user")
                    .header("citrus_http_context_path", "")
                    .header("citrus_http_query_params", "id=${id}&name=TestUser")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .header("citrus_http_status_code", "200")
            .header("citrus_http_version", "HTTP/1.1")
            .header("citrus_http_reason_phrase", "OK");
    }
}