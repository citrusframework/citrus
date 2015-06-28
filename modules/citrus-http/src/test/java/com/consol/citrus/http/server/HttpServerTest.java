/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.http.server;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpEndpointConfiguration;
import com.consol.citrus.http.message.HttpMessage;
import com.consol.citrus.http.message.HttpMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Simple unit test for HttpServer
 * @author jza
 */
public class HttpServerTest extends AbstractTestNGUnitTest {

    private int port = 8095;
    private String uri = "http://localhost:" + port + "/test";

    private HttpClient client;

    @Autowired
    private TestContextFactory testContextFactory;

    @BeforeClass
    public void setupClient() {
        HttpEndpointConfiguration endpointConfiguration = new HttpEndpointConfiguration();
        endpointConfiguration.setRequestUrl(uri);
        client = new HttpClient(endpointConfiguration);
    }

    @Test
    public void startupAndShutdownTest() throws IOException {
        HttpServer server = new HttpServer();
        server.setPort(port);
        server.setApplicationContext(applicationContext);
        server.setContextConfigLocation("classpath:com/consol/citrus/http/HttpServerTest-http-servlet.xml");

        server.startup();

        TestContext context = testContextFactory.getObject();
        client.send(new HttpMessage("Hello")
                .method(HttpMethod.GET), context);

        //assert get was successful
        Assert.assertEquals(client.receive(context).getHeader(HttpMessageHeaders.HTTP_STATUS_CODE), HttpStatus.OK.value());

        server.shutdown();

        try {
            client.send(new HttpMessage("Should fail")
                    .method(HttpMethod.GET), context);

            Assert.fail("Server supposed to be in shut down state, but was accessible via client request");
        } catch (ResourceAccessException e) {
            Assert.assertTrue(e.getMessage().contains("Connection refused"));
        }
    }
}
