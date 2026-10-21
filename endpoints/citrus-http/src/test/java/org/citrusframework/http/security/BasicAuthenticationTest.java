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

package org.citrusframework.http.security;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.http.client.HttpClient;
import org.eclipse.jetty.security.SecurityHandler;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicAuthenticationTest {

    @Test
    public void shouldDescribeServerAuthentication() {
        BasicAuthentication authentication = new BasicAuthentication("citrus", "secr3t")
                .realm("citrus-realm")
                .userRoles("admin", "user");

        Optional<HttpServerAuthentication> serverAuthentication = authentication.getServerAuthentication("/secured/*");

        assertThat(serverAuthentication).isPresent();

        HttpServerAuthentication description = serverAuthentication.get();
        assertThat(description.realm()).isEqualTo("citrus-realm");
        assertThat(description.users()).hasSize(1);
        assertThat(description.users().get(0).getName()).isEqualTo("citrus");
        assertThat(description.users().get(0).getPassword()).isEqualTo("secr3t");
        assertThat(description.users().get(0).getRoles()).containsExactly("admin", "user");
        assertThat(description.constraints()).containsOnlyKeys("/secured/*");
        assertThat(description.constraints().get("/secured/*")).containsExactly("admin", "user");
    }

    @Test
    public void shouldUseDefaultRealmAndRoles() {
        BasicAuthentication authentication = new BasicAuthentication("citrus", "secr3t");

        HttpServerAuthentication description = authentication.getServerAuthentication("/*").orElseThrow();

        assertThat(description.realm()).isEmpty();
        assertThat(description.users().get(0).getRoles()).containsExactly("citrus");
    }

    @Test
    public void shouldNotDescribeServerAuthenticationByDefault() {
        HttpAuthentication authentication = new HttpAuthentication() {
            @Override
            public SecurityHandler getSecurityHandler(String resourcePath) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ClientHttpRequestFactory getRequestFactory(String requestUrl, HttpClient httpClient) {
                throw new UnsupportedOperationException();
            }
        };

        assertThat(authentication.getServerAuthentication("/secured/*")).isEmpty();
    }

    @Test
    public void shouldStillCreateJettySecurityHandler() {
        BasicAuthentication authentication = new BasicAuthentication("citrus", "secr3t");

        SecurityHandler securityHandler = authentication.getSecurityHandler("/secured/*");

        assertThat(securityHandler).isNotNull();
    }

    @Test
    public void shouldDeprecateJettySecurityHandlerFactory() throws Exception {
        Method method = HttpAuthentication.class.getMethod("getSecurityHandler", String.class);

        assertThat(method.isAnnotationPresent(Deprecated.class)).isTrue();
        assertThat(method.getAnnotation(Deprecated.class).since()).isEqualTo("5.2");
    }

    @Test
    public void shouldDescribeUsersAndConstraintsAsLists() {
        HttpServerAuthentication description = new HttpServerAuthentication(
                "realm",
                List.of(new User("citrus", "secr3t", new String[]{"citrus"})),
                Map.of("/*", List.of("citrus")));

        assertThat(description.realm()).isEqualTo("realm");
        assertThat(description.users()).hasSize(1);
        assertThat(description.constraints().get("/*")).containsExactly("citrus");
    }
}
