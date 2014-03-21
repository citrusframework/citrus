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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
                .http()
                .uri("http://localhost:8072")
                .method(HttpMethod.GET)
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                    receive("httpRequestReceiver")
                            .header("Host", "localhost:8072")
                            .header("Content-Type", "text/html;charset=ISO-8859-1")
                            .header("Accept", "application/xml;charset=UTF-8")
                            .http()
                            .method(HttpMethod.GET)
                            .uri("/")
                            .contextPath("")
                            .queryParam("", "")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .http()
            .status(HttpStatus.OK)
            .version("HTTP/1.1");

        
        echo("Use context path variables.");
        
        parallel(
            send("httpMessageSender")
                .http()
                .uri("http://localhost:8072/test/user/${id}")
                .method(HttpMethod.GET)
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                    receive("httpRequestReceiver")
                            .http()
                            .header("Host", "localhost:8072")
                            .header("Content-Type", "text/html;charset=ISO-8859-1")
                            .header("Accept", "application/xml;charset=UTF-8")
                            .method(HttpMethod.GET)
                            .uri("/test/user/${id}")
                            .contextPath("")
                            .queryParam("", "")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .http()
            .status(HttpStatus.OK)
            .version("HTTP/1.1");
        
        echo("Use query parameter and context path variables.");
        
        parallel(
            send("httpMessageSender")
                .http()
                .uri("http://localhost:8072/test")
                .method(HttpMethod.GET)
                .path("user")
                .queryParam("id", "${id}")
                .queryParam("name", "TestUser")
                .header("Content-Type", "text/html")
                .header("Accept", "application/xml;charset=UTF-8"),
                
            sequential(
                receive("httpRequestReceiver")
                    .http()
                    .header("Host", "localhost:8072")
                    .header("Content-Type", "text/html;charset=ISO-8859-1")
                    .header("Accept", "application/xml;charset=UTF-8")
                    .method(HttpMethod.GET)
                    .uri("/test/user")
                    .contextPath("")
                    .queryParam("id", "${id}")
                    .queryParam("name", "TestUser")
            )
        );
        
        receive("httpResponseReceiver")
            .timeout(2000L)
            .http()
            .status(HttpStatus.OK)
            .version("HTTP/1.1");
    }
}