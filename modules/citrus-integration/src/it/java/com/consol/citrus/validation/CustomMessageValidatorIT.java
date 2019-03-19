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

package com.consol.citrus.validation;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.config.annotation.HttpClientConfig;
import com.consol.citrus.http.config.annotation.HttpServerConfig;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class CustomMessageValidatorIT extends TestNGCitrusTestRunner {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:7177")
    private HttpClient wsClient;

    @CitrusEndpoint
    @HttpServerConfig(port = 7177, autoStart = true)
    private HttpServer wsServer;

    @Test(groups = "com.consol.citrus.ShouldFailGroup", expectedExceptions = TestCaseFailedException.class)
    @CitrusTest
    public void test() {
        http(action -> action.client(wsClient)
                .send()
                .post("/")
                .contentType("application/xml")
                .payload("<doc text=\"hello\"/>")
                .fork(true));

        http(action -> action.server(wsServer)
                .receive()
                .post("/")
                .contentType("application/xml")
                .validator(new DomXmlMessageValidator(), new XpathMessageValidator())
                .validate("//doc/@text", "nothello"));
    }

}
