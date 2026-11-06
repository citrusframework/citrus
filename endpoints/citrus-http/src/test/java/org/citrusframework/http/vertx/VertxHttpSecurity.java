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
import java.util.Objects;
import java.util.Set;
import org.citrusframework.http.message.HttpMessage;

/** Immutable Basic login and first-match authorization rules for the test-only server. */
public final class VertxHttpSecurity {
    public static final String PRINCIPAL_HEADER = "citrus_vertx_principal";
    public static final String ROLES_HEADER = "citrus_vertx_roles";

    /** Return an authenticated identity, or null for invalid credentials. Implementations must bound external I/O. */
    @FunctionalInterface
    public interface LoginService { Identity login(String username, String password) throws Exception; }

    public record Identity(String username, Set<String> roles) {
        public Identity {
            if (username == null || username.isBlank()) throw new IllegalArgumentException("Identity needs a username");
            roles = Set.copyOf(roles);
            if (roles.stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("Roles must not be blank");
        }
    }
    public enum Access { PERMIT_ALL, AUTHENTICATED, ANY_ROLE, DENY_ALL }

    /** Full request path, exact/prefix/extension mapping, optional HTTP methods, and required access. */
    public record Rule(String path, Set<String> methods, Access access, Set<String> roles) {
        public Rule {
            new VertxHttpRoute("/", path);
            methods = Set.copyOf(methods);
            if (methods.stream().anyMatch(method -> !method.matches("[A-Z]+")))
                throw new IllegalArgumentException("HTTP methods must be uppercase tokens");
            Objects.requireNonNull(access);
            roles = Set.copyOf(roles);
            if (roles.stream().anyMatch(String::isBlank) || (access == Access.ANY_ROLE) != !roles.isEmpty())
                throw new IllegalArgumentException("Only ANY_ROLE rules require a nonempty role set");
        }
        boolean matches(String path, String method) {
            return (methods.isEmpty() || methods.contains(method)) && new VertxHttpRoute("/", this.path).status(path) == 200;
        }
    }
    record Decision(int status, Identity identity) { }
    private final LoginService loginService;
    private final List<Rule> rules;
    private final String challenge;

    public VertxHttpSecurity(String realm, LoginService loginService, List<Rule> rules) {
        challenge = new VertxBasicAuthentication("realm-validator", "", realm).challenge();
        this.loginService = Objects.requireNonNull(loginService);
        this.rules = List.copyOf(rules);
    }
    String challenge() { return challenge; }

    Decision authorize(String path, String method, List<String> authorization) throws Exception {
        Rule rule = rules.stream().filter(candidate -> candidate.matches(path, method)).findFirst().orElse(null);
        if (rule == null || rule.access() == Access.DENY_ALL) return new Decision(403, null);
        if (rule.access() == Access.PERMIT_ALL) return new Decision(200, null);
        if (authorization.size() != 1) return new Decision(401, null);
        String value = authorization.get(0);
        int space = value.indexOf(' ');
        if (space < 0 || !value.substring(0, space).equalsIgnoreCase("Basic")) return new Decision(401, null);
        int start = space;
        while (start < value.length() && value.charAt(start) == ' ') start++;
        byte[] decoded;
        try { decoded = Base64.getDecoder().decode(value.substring(start)); }
        catch (IllegalArgumentException e) { return new Decision(401, null); }
        for (byte character : decoded) {
            if (character < 32 || character > 126) return new Decision(401, null);
        }
        String credentials = new String(decoded, StandardCharsets.US_ASCII);
        int colon = credentials.indexOf(':');
        if (colon <= 0) return new Decision(401, null);
        Identity identity = loginService.login(credentials.substring(0, colon), credentials.substring(colon + 1));
        if (identity == null) return new Decision(401, null);
        if (rule.access() == Access.ANY_ROLE && rule.roles().stream().noneMatch(identity.roles()::contains))
            return new Decision(403, null);
        return new Decision(200, identity);
    }

    static void applyIdentity(HttpMessage request, Identity identity) {
        for (String name : List.copyOf(request.getHeaders().keySet())) {
            if (name.equalsIgnoreCase(PRINCIPAL_HEADER) || name.equalsIgnoreCase(ROLES_HEADER)) request.removeHeader(name);
        }
        if (identity != null) {
            request.setHeader(PRINCIPAL_HEADER, identity.username());
            request.setHeader(ROLES_HEADER, identity.roles());
        }
    }
}
