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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.citrusframework.http.message.HttpMessage;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class VertxHttpSecurityTest {
    static String basic(String value) { return "Basic " + Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.US_ASCII)); }
    static VertxHttpSecurity.Rule rule(String path, Set<String> methods, VertxHttpSecurity.Access access, String... roles) {
        return new VertxHttpSecurity.Rule(path, methods, access, Set.of(roles));
    }
    @Test public void enforcesRolesMethodsAndDefaultDeny() throws Exception {
        var security = new VertxHttpSecurity("tests", (name, password) -> password.equals("secret")
                ? new VertxHttpSecurity.Identity(name, Set.of("reader")) : null, List.of(
                rule("/api/*", Set.of("GET"), VertxHttpSecurity.Access.ANY_ROLE, "reader", "admin"),
                rule("/api/*", Set.of(), VertxHttpSecurity.Access.ANY_ROLE, "admin")));
        assertEquals(security.authorize("/api/items", "GET", List.of(basic("alice:secret"))).status(), 200);
        assertEquals(security.authorize("/api/items", "POST", List.of(basic("alice:secret"))).status(), 403);
        assertEquals(security.authorize("/other", "GET", List.of(basic("alice:secret"))).status(), 403);
        assertEquals(security.authorize("/api/items", "GET", List.of()).status(), 401);
        assertEquals(security.authorize("/api/items", "GET", List.of(basic("alice:wrong"))).status(), 401);
    }
    @Test public void bypassesLoginForExplicitPublicAndDeniedRules() throws Exception {
        var security = new VertxHttpSecurity("tests", (name, password) -> { throw new AssertionError(); }, List.of(
                rule("/health", Set.of(), VertxHttpSecurity.Access.PERMIT_ALL),
                rule("/*", Set.of(), VertxHttpSecurity.Access.DENY_ALL)));
        assertEquals(security.authorize("/health", "GET", List.of("invalid")).status(), 200);
        assertNull(security.authorize("/health", "GET", List.of(basic("alice:secret"))).identity());
        assertEquals(security.authorize("/other", "GET", List.of()).status(), 403);
    }
    @Test public void rejectsMalformedOrDuplicateCredentialsBeforeLogin() throws Exception {
        var calls = new AtomicInteger();
        var security = new VertxHttpSecurity("tests", (name, password) -> { calls.incrementAndGet(); return null; },
                List.of(rule("/*", Set.of(), VertxHttpSecurity.Access.AUTHENTICATED)));
        for (var headers : List.of(List.of("Basic %%%"), List.of(basic("no-colon")), List.of(basic(":password")),
                List.of(basic("user:pass"), basic("user:pass")), List.of("Bearer token")))
            assertEquals(security.authorize("/", "GET", headers).status(), 401);
        assertEquals(calls.get(), 0);
    }
    @Test public void usesCanonicalPathsAndCannotTrustClientIdentityHeaders() throws Exception {
        var security = new VertxHttpSecurity("tests", (name, password) -> new VertxHttpSecurity.Identity("alice", Set.of("user")),
                List.of(rule("/admin/*", Set.of(), VertxHttpSecurity.Access.ANY_ROLE, "admin"),
                        rule("/*", Set.of(), VertxHttpSecurity.Access.PERMIT_ALL)));
        for (String path : List.of("/admin/x", "/%61dmin/x", "/admin;v=1/x"))
            assertEquals(security.authorize(path, "GET", List.of(basic("alice:secret"))).status(), 403);
        var request = new HttpMessage(); request.setHeader("CITRUS_VERTX_PRINCIPAL", "admin");
        request.setHeader(VertxHttpSecurity.ROLES_HEADER, Set.of("admin"));
        VertxHttpSecurity.applyIdentity(request, new VertxHttpSecurity.Identity("alice", Set.of("user")));
        assertNull(request.getHeader("CITRUS_VERTX_PRINCIPAL"));
        assertEquals(request.getHeader(VertxHttpSecurity.PRINCIPAL_HEADER), "alice");
        assertEquals(request.getHeader(VertxHttpSecurity.ROLES_HEADER), Set.of("user"));
        VertxHttpSecurity.applyIdentity(request, null);
        assertNull(request.getHeader(VertxHttpSecurity.PRINCIPAL_HEADER));
    }
    @Test public void snapshotsRulesAndAuthenticatesUsersWithoutRoles() throws Exception {
        var rules = new java.util.ArrayList<VertxHttpSecurity.Rule>();
        rules.add(rule("/*", Set.of(), VertxHttpSecurity.Access.AUTHENTICATED));
        var security = new VertxHttpSecurity("tests", (name, password) -> new VertxHttpSecurity.Identity(name, Set.of()), rules);
        rules.clear();
        var result = security.authorize("/", "GET", List.of(basic("alice:secret")));
        assertEquals(result.status(), 200); assertEquals(result.identity().username(), "alice");
        assertTrue(result.identity().roles().isEmpty());
        expectThrows(IllegalArgumentException.class, () -> rule("/*", Set.of(), VertxHttpSecurity.Access.ANY_ROLE));
        expectThrows(IllegalArgumentException.class, () -> rule("/*", Set.of("get"), VertxHttpSecurity.Access.AUTHENTICATED));
    }

}
