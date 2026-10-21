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

package org.citrusframework.http.server.vertx;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.citrusframework.http.security.HttpServerAuthentication;
import org.citrusframework.http.security.User;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the basic authentication handler of the Vert.x based Http server.
 */
public class VertxBasicAuthHandlerTest extends AbstractTestNGUnitTest {

    private static final String REALM = "citrus-realm";
    private static final String SECURED_PATH = "/secured/*";

    private final VertxBasicAuthHandler handler = new VertxBasicAuthHandler("/", createAuthentication());

    @Test
    public void shouldAllowRequestOnUnsecuredPath() {
        assertThat(handler.authenticate("/orders", null)).isEqualTo(VertxBasicAuthHandler.Outcome.ALLOWED);
    }

    @Test
    public void shouldRejectRequestWithoutCredentials() {
        assertThat(handler.authenticate("/secured/orders", null)).isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
    }

    @Test
    public void shouldRejectRequestWithWrongPassword() {
        assertThat(handler.authenticate("/secured/orders", basicAuthorization("citrus", "wrong")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
    }

    @Test
    public void shouldRejectRequestWithUnknownUser() {
        assertThat(handler.authenticate("/secured/orders", basicAuthorization("unknown", "secr3t")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
    }

    @Test
    public void shouldRejectRequestWithMalformedCredentials() {
        assertThat(handler.authenticate("/secured/orders", "Basic not-base64!"))
                .isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
        assertThat(handler.authenticate("/secured/orders", basicAuthorizationWithoutSeparator()))
                .isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
    }

    @Test
    public void shouldRejectRequestWithUnsupportedAuthenticationScheme() {
        assertThat(handler.authenticate("/secured/orders", "Bearer token"))
                .isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
    }

    @Test
    public void shouldAllowRequestWithValidCredentials() {
        assertThat(handler.authenticate("/secured/orders", basicAuthorization("citrus", "secr3t")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.ALLOWED);
    }

    @Test
    public void shouldRejectRequestWithMissingRole() {
        assertThat(handler.authenticate("/secured/orders", basicAuthorization("guest", "guest")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.FORBIDDEN);
    }

    @Test
    public void shouldUseMostSpecificConstraint() {
        VertxBasicAuthHandler handler = new VertxBasicAuthHandler("/", createAuthentication(
                Map.of(SECURED_PATH, List.of("citrus"), "/secured/admin/*", List.of("admin"))));

        assertThat(handler.authenticate("/secured/orders", basicAuthorization("citrus", "secr3t")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.ALLOWED);
        assertThat(handler.authenticate("/secured/admin/orders", basicAuthorization("citrus", "secr3t")))
                .isEqualTo(VertxBasicAuthHandler.Outcome.FORBIDDEN);
    }

    @Test
    public void shouldMatchPathRelativeToContextPath() {
        VertxBasicAuthHandler handler = new VertxBasicAuthHandler("/api", createAuthentication());

        assertThat(handler.authenticate("/api/secured/orders", null)).isEqualTo(VertxBasicAuthHandler.Outcome.UNAUTHORIZED);
        assertThat(handler.authenticate("/api/orders", null)).isEqualTo(VertxBasicAuthHandler.Outcome.ALLOWED);
    }

    private HttpServerAuthentication createAuthentication() {
        return createAuthentication(Map.of(SECURED_PATH, List.of("citrus")));
    }

    private HttpServerAuthentication createAuthentication(Map<String, List<String>> constraints) {
        List<User> users = List.of(
                new User("citrus", "secr3t", new String[]{"citrus"}),
                new User("guest", "guest", new String[]{"guest"}));

        return new HttpServerAuthentication(REALM, users, constraints);
    }

    private String basicAuthorization(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    private String basicAuthorizationWithoutSeparator() {
        return "Basic " + Base64.getEncoder().encodeToString("citrus".getBytes(StandardCharsets.UTF_8));
    }
}
