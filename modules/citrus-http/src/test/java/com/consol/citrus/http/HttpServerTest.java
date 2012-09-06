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

package com.consol.citrus.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.mortbay.jetty.HttpHeaders;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGUnitTest;

/**
 * Simple unit test for HttpServer
 * @author jza
 */
public class HttpServerTest extends AbstractTestNGUnitTest {

    @Test
    public void startupAndShutdownTest() throws IOException {
        HttpServer server = new HttpServer();
        server.setPort(8095);
        server.setApplicationContext(applicationContext);
        server.setContextConfigLocation("classpath:com/consol/citrus/http/HttpServerTest-http-servlet.xml");

        server.startup();

        //build a client to test the server
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet get = new HttpGet("http://localhost:8095/test");
        get.setHeader(HttpHeaders.ACCEPT, "text/xml");
        get.setHeader(HttpHeaders.CONTENT_TYPE, "text/xml");
        //send get request
        HttpResponse res = httpclient.execute(get);
        //assert get was successful
        Assert.assertTrue(res.getStatusLine().getStatusCode() == 200);
        //read the response (otherwise conn is not released)
        InputStream resStream = res.getEntity().getContent();
        resStream.close();

        server.shutdown();

        try {
            httpclient.execute(get);
            Assert.fail("Server shutdown did not work!!");
        } catch (HttpHostConnectException e) {
          //fine, we expected this
        }
    }
}
