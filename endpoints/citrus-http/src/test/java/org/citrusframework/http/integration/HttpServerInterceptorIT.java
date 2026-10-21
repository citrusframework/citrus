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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * Verifies that a transport neutral {@link org.citrusframework.http.interceptor.HttpServerInterceptor}
 * registered via the Spring XML configuration is applied on the Jetty based Http server as well.
 */
@Test
public class HttpServerInterceptorIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    @CitrusTest
    public void shouldApplyServerInterceptorOnJettyServer() {
        when(http().client("serverInterceptorHttpClient")
                .send()
                .post()
                .message()
                .body("<testRequestMessage><text>Hello HttpServer</text></testRequestMessage>"));

        then(http().client("serverInterceptorHttpClient")
                .receive()
                .response(HttpStatus.OK)
                .message()
                .header("X-Citrus-Server-Interceptor", "applied"));
    }
}
