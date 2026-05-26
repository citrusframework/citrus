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

package org.citrusframework.integration.container;

import com.sun.net.httpserver.HttpServer;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * @since 2.4
 */
public class WaitIT extends TestNGCitrusSpringSupport {

    private HttpServer server;

    @BeforeClass
    public void startHttpServer() throws IOException {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            throw new SkipException("Skipped on Windows - com.sun.net.httpserver.HttpServer does not reliably serve HTTP responses on Windows");
        }

        server = HttpServer.create(new InetSocketAddress("localhost", 8001), 0);
        server.createContext("/test", httpExchange -> httpExchange.sendResponseHeaders(200, 0));
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterClass(alwaysRun = true)
    public void stopHttpServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @CitrusTestSource(type = TestLoader.SPRING, name = "WaitIT")
    public void waitIT() {}
}
