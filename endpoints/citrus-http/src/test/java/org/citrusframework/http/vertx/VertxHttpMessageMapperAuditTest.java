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

package org.citrusframework.http.vertx;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.UrlPathHelper;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpMessageMapperAuditTest {
    @Test
    public void preservesCookieAttributes() {
        var config = new HttpEndpointConfiguration(); config.setHandleCookies(true);
        var cookie = new Cookie("token", "value"); cookie.setMaxAge(0);
        cookie.setSecure(true); cookie.setHttpOnly(true); cookie.setAttribute("SameSite", "Strict");
        var message = new HttpMessage("ok"); message.cookie(cookie);
        String header = VertxHttpMessageMapper.toResponsePlan(message, config, false).headers().get("Set-Cookie").get(0);
        for (String attribute : List.of("Max-Age=0", "Secure", "HttpOnly", "SameSite=Strict")) {
            assertTrue(header.contains(attribute), header);
        }
    }
    @Test
    public void responseDoesNotMixFramingHeaders() {
        var message = new HttpMessage("ok"); message.setHeader("Transfer-Encoding", "chunked");
        var plan = VertxHttpMessageMapper.toResponsePlan(message, new HttpEndpointConfiguration(), false);
        assertFalse(plan.headers().keySet().stream().anyMatch("Transfer-Encoding"::equalsIgnoreCase));
        assertEquals(plan.headers().get("Content-Length"), List.of("2"));
    }
    @Test
    public void pathMatchesJettyControllerNormalization() {
        for (String path : List.of("/hello%20world", "/hello;key=a,b/next", "/a//b")) {
            var servletRequest = new MockHttpServletRequest("GET", path);
            var snapshot = new VertxHttpMessageMapper.RequestSnapshot("GET", path, path, null,
                    "HTTP/1.1", Map.of(), null, "audit", 1);
            assertEquals(VertxHttpMessageMapper.toHttpMessage(snapshot, new HttpEndpointConfiguration()).getPath(),
                    new UrlPathHelper().getRequestUri(servletRequest));
        }
    }
}
