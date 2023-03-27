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

package org.citrusframework.citrus.http.integration;

import org.citrusframework.citrus.annotations.CitrusEndpoint;
import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.exceptions.ValidationException;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.config.annotation.HttpClientConfig;
import org.citrusframework.citrus.http.config.annotation.HttpServerConfig;
import org.citrusframework.citrus.http.server.HttpServer;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.citrus.validation.xml.DomXmlMessageValidator;
import org.citrusframework.citrus.validation.xml.XpathMessageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.actions.StopServerAction.Builder.stop;
import static org.citrusframework.citrus.container.Assert.Builder.assertException;
import static org.citrusframework.citrus.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.citrus.dsl.XpathSupport.xpath;
import static org.citrusframework.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class CustomMessageValidatorIT extends TestNGCitrusSpringSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:7177")
    private HttpClient httpClient;

    @CitrusEndpoint
    @HttpServerConfig(port = 7177, autoStart = true)
    private HttpServer httpServer;

    @Test
    @CitrusTest
    public void test() {
        this.getClass().getResource("/citrus.properties");

        given(doFinally().actions(
                stop(httpServer)
        ));

        when(http().client(httpClient)
                .send()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body("<doc text=\"hello\"/>")
                .fork(true));

        then(http().server(httpServer)
                .receive()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .validators(new DomXmlMessageValidator(), new XpathMessageValidator())
                .validate(xpath()
                        .expression("//doc/@text", "hello")));

        then(http().server(httpServer)
                .send()
                .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testFailure() {
        then(doFinally().actions(
                stop(httpServer)
        ));

        when(http().client(httpClient)
                .send()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body("<doc text=\"hello\"/>")
                .fork(true));

        then(assertException()
            .exception(ValidationException.class)
            .when(http().server(httpServer)
                .receive()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .validators(new DomXmlMessageValidator(), new XpathMessageValidator())
                .validate(xpath()
                        .expression("//doc/@text", "nothello"))));
    }

}
