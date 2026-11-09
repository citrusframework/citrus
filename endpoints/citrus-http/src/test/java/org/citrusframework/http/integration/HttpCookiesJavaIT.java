/*
 * Copyright the original author or authors.
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

package org.citrusframework.http.integration;

import jakarta.servlet.http.Cookie;

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.junit.jupiter.api.parallel.Isolated;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
@Isolated("HTTP ITs share singleton endpoints and Citrus Spring context state under parallel classes")
public class HttpCookiesJavaIT implements TestActionSupport {

    @Test
    @CitrusTest
    public void httpCookies(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(createVariable("correlationId", "citrus:randomNumber(10)"));
        runner.given(createVariable("messageId", "citrus:randomNumber(10)"));
        runner.given(createVariable("user", "Christoph"));

        Cookie cookie = new Cookie("Token", "${messageId}");
        cookie.setPath("/test/cookie.py");
        cookie.setSecure(false);
        cookie.setDomain("citrusframework.org");
        cookie.setMaxAge(86400);

        runner.given(http().client("echoHttpClient")
            .send()
            .post()
            .fork(true)
            .message()
            .body("<HelloRequest>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello Citrus</Text>" +
                    "</HelloRequest>")
            .cookie(new Cookie("Token", "${messageId}")));

        runner.when(http().server("echoHttpServer")
            .receive()
            .post()
            .message()
            .body("<HelloRequest>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>${user}</User>" +
                        "<Text>Hello Citrus</Text>" +
                    "</HelloRequest>")
            .cookie(new Cookie("Token", "${messageId}")));

       runner.then(http().server("echoHttpServer")
           .send()
           .response(HttpStatus.OK.value())
           .message()
           .body("<HelloResponse>" +
                       "<MessageId>${messageId}</MessageId>" +
                       "<CorrelationId>${correlationId}</CorrelationId>" +
                       "<User>HelloService</User>" +
                       "<Text>Hello ${user}</Text>" +
                   "</HelloResponse>")
           .cookie(cookie));

       runner.then(http().client("echoHttpClient")
            .receive()
            .response(HttpStatus.OK)
            .message()
            .body("<HelloResponse>" +
                        "<MessageId>${messageId}</MessageId>" +
                        "<CorrelationId>${correlationId}</CorrelationId>" +
                        "<User>HelloService</User>" +
                        "<Text>Hello ${user}</Text>" +
                    "</HelloResponse>")
            .cookie(cookie));
    }
}
