/*
 * Copyright 2006-2017 the original author or authors.
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

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;

/**
 * @author Christoph Deppisch
 */
@Test
public class HttpCookiesJavaIT extends TestNGCitrusTestDesigner {
    
    @CitrusTest
    public void httpCookies() {
        variable("correlationId", "citrus:randomNumber(10)");
        variable("messageId", "citrus:randomNumber(10)");
        variable("user", "Christoph");

        Cookie cookie = new Cookie("Token", "${messageId}");
        cookie.setPath("/test/cookie.py");
        cookie.setSecure(false);
        cookie.setDomain("citrusframework.org");
        cookie.setMaxAge(86400);

        http().client("echoHttpClient")
            .send()
            .post()
            .fork(true)
            .payload("<HelloRequest>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello Citrus</Text>" +
                    "</HelloRequest>")
            .cookie(new Cookie("Token", "${messageId}"));

        http().server("echoHttpServer")
            .receive()
            .post()
            .payload("<HelloRequest>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello Citrus</Text>" +
                    "</HelloRequest>")
            .cookie(new Cookie("Token", "${messageId}"));

       http().server("echoHttpServer")
           .send()
           .response(HttpStatus.OK)
           .payload("<HelloResponse>" +
                       "<MessageId>${messageId}</MessageId>" +
                       "<CorrelationId>${correlationId}</CorrelationId>" +
                       "<User>HelloService</User>" +
                       "<Text>Hello ${user}</Text>" +
                   "</HelloResponse>")
           .cookie(cookie);

        http().client("echoHttpClient")
            .receive()
            .response(HttpStatus.OK)
            .payload("<HelloResponse>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>HelloService</User>" +
                        "<Text>Hello ${user}</Text>" +
                    "</HelloResponse>")
            .cookie(cookie);
    }
}