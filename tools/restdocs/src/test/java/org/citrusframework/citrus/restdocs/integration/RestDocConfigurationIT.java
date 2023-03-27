/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.citrus.restdocs.integration;

import java.util.Arrays;

import org.citrusframework.citrus.annotations.CitrusTest;
import org.citrusframework.citrus.http.client.HttpClient;
import org.citrusframework.citrus.http.client.HttpClientBuilder;
import org.citrusframework.citrus.report.TestListeners;
import org.citrusframework.citrus.restdocs.http.CitrusRestDocConfigurer;
import org.citrusframework.citrus.restdocs.http.RestDocClientInterceptor;
import org.citrusframework.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.restdocs.ManualRestDocumentation;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.http.actions.HttpActionBuilder.http;
import static org.citrusframework.citrus.restdocs.http.CitrusRestDocsSupport.restDocsConfigurer;
import static org.citrusframework.citrus.restdocs.http.CitrusRestDocsSupport.restDocsInterceptor;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocConfigurationIT extends TestNGCitrusSpringSupport {

    @Autowired
    private TestListeners testListeners;

    private HttpClient httpClient;

    @BeforeClass
    public void setup() {
        CitrusRestDocConfigurer restDocConfigurer = restDocsConfigurer(new ManualRestDocumentation("target/generated-snippets"));
        RestDocClientInterceptor restDocInterceptor = restDocsInterceptor("rest-docs/{method-name}");

        httpClient = new HttpClientBuilder()
                                    .requestUrl("http://localhost:11080/hello")
                                    .requestMethod(RequestMethod.POST)
                                    .contentType(MediaType.APPLICATION_XML_VALUE)
                                    .interceptors(Arrays.asList(restDocConfigurer, restDocInterceptor))
                                    .build();

        testListeners.addTestListener(restDocConfigurer);
    }

    @Test
    @CitrusTest
    public void testRestDocs() {
        when(http().client(httpClient)
                .send()
                .post()
                .message()
                .body("<HelloRequest xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<MessageId>1234567890</MessageId>" +
                    "<CorrelationId>1000000001</CorrelationId>" +
                    "<User>User</User>" +
                    "<Text>Hello Citrus</Text>" +
                "</HelloRequest>")
                .header("Operation", "sayHello"));

        then(http().client(httpClient)
                .receive()
                .response(HttpStatus.OK)
                .message()
                .body("<HelloResponse xmlns=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<MessageId>1234567890</MessageId>" +
                    "<CorrelationId>1000000001</CorrelationId>" +
                    "<User>HelloService</User>" +
                    "<Text>Hello User</Text>" +
                "</HelloResponse>")
                .header("Operation", "sayHello"));
    }
}
