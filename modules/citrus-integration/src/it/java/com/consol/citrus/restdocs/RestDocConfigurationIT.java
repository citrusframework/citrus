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

package com.consol.citrus.restdocs;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.endpoint.CitrusEndpoints;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.restdocs.http.CitrusRestDocConfigurer;
import com.consol.citrus.restdocs.http.RestDocClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.restdocs.ManualRestDocumentation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.consol.citrus.restdocs.http.CitrusRestDocsSupport.restDocsConfigurer;
import static com.consol.citrus.restdocs.http.CitrusRestDocsSupport.restDocsInterceptor;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class RestDocConfigurationIT extends TestNGCitrusTestDesigner {

    @Autowired
    private TestListeners testListeners;

    private HttpClient httpClient;

    @BeforeClass
    public void setup() {
        CitrusRestDocConfigurer restDocConfigurer = restDocsConfigurer(new ManualRestDocumentation("target/generated-snippets"));
        RestDocClientInterceptor restDocInterceptor = restDocsInterceptor("rest-docs/{method-name}");

        httpClient = CitrusEndpoints.http()
                                    .client()
                                    .requestUrl("http://localhost:8073/test")
                                    .requestMethod(HttpMethod.POST)
                                    .contentType("text/xml")
                                    .interceptors(Arrays.asList(restDocConfigurer, restDocInterceptor))
                                    .build();

        testListeners.addTestListener(restDocConfigurer);
    }

    @Test
    @CitrusTest
    public void testRestDocs() {
        http().client(httpClient)
                .post()
                .payload("<testRequestMessage>" +
                            "<text>Hello HttpServer</text>" +
                        "</testRequestMessage>");

        http().client(httpClient)
                .response(HttpStatus.OK)
                .payload("<testResponseMessage>" +
                            "<text>Hello TestFramework</text>" +
                        "</testResponseMessage>");
    }
}
