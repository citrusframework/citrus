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
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

import org.citrusframework.http.security.HttpServerAuthentication;
import org.citrusframework.http.security.User;
import org.citrusframework.http.server.HttpPathSpec;

import io.vertx.ext.web.RoutingContext;

/**
 * Handler performing the basic authentication of incoming requests. The handler is invoked before
 * the request body is read so that unauthorized requests never reach the Citrus endpoint adapter.
 *
 * @since 5.2
 */
class VertxBasicAuthHandler {

    /** Outcome of the authentication. */
    enum Outcome {
        /** The request is authenticated and may be processed. */
        ALLOWED,
        /** The request is not authenticated and is answered with a challenge. */
        UNAUTHORIZED,
        /** The request is authenticated but the user is missing a required role. */
        FORBIDDEN
    }

    private static final String BASIC_PREFIX = "Basic ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String WWW_AUTHENTICATE_HEADER = "WWW-Authenticate";

    private final String contextPath;
    private final HttpServerAuthentication authentication;
    private final List<Constraint> constraints;

    /**
     * Constructor.
     *
     * @param contextPath the context path of the server the handler belongs to.
     * @param authentication the transport neutral server authentication.
     */
    VertxBasicAuthHandler(String contextPath, HttpServerAuthentication authentication) {
        this.contextPath = contextPath;
        this.authentication = authentication;
        this.constraints = authentication.constraints().entrySet().stream()
                .map(entry -> new Constraint(HttpPathSpec.parse(entry.getKey()), entry.getValue()))
                .sorted(Comparator.comparingInt(Constraint::getSpecificity).reversed())
                .toList();
    }

    /**
     * Authenticates the given request. Requests matching no secured path are passed without
     * authentication.
     *
     * @param requestPath the path of the request.
     * @param authorizationHeader the authorization header of the request.
     * @return the outcome of the authentication.
     */
    Outcome authenticate(String requestPath, String authorizationHeader) {
        List<String> requiredRoles = getRequiredRoles(getRelativePath(requestPath));

        if (requiredRoles == null) {
            return Outcome.ALLOWED;
        }

        Credentials credentials = parseCredentials(authorizationHeader);
        if (credentials == null) {
            return Outcome.UNAUTHORIZED;
        }

        User user = findUser(credentials.username());
        if (user == null || !isPasswordMatching(user.getPassword(), credentials.password())) {
            return Outcome.UNAUTHORIZED;
        }

        if (!hasRequiredRole(user, requiredRoles)) {
            return Outcome.FORBIDDEN;
        }

        return Outcome.ALLOWED;
    }

    /**
     * Authenticates the request of the given routing context and answers unauthorized or forbidden
     * requests with the matching status code.
     *
     * @param context the routing context of the request.
     * @return the outcome of the authentication.
     */
    Outcome handle(RoutingContext context) {
        Outcome outcome = authenticate(context.request().path(),
                context.request().getHeader(AUTHORIZATION_HEADER));

        if (outcome == Outcome.UNAUTHORIZED) {
            context.response()
                    .setStatusCode(401)
                    .putHeader(WWW_AUTHENTICATE_HEADER, "Basic realm=\"%s\"".formatted(authentication.realm()))
                    .end();
        } else if (outcome == Outcome.FORBIDDEN) {
            context.response().setStatusCode(403).end();
        }

        return outcome;
    }

    /**
     * Gets the path of the request relative to the context path of the server.
     *
     * @param requestPath the path of the request.
     * @return the path relative to the context path.
     */
    private String getRelativePath(String requestPath) {
        if (requestPath == null) {
            return null;
        }

        if (contextPath == null || "/".equals(contextPath)) {
            return requestPath;
        }

        if (requestPath.startsWith(contextPath)) {
            String relativePath = requestPath.substring(contextPath.length());
            return relativePath.isEmpty() ? "/" : relativePath;
        }

        return requestPath;
    }

    /**
     * Gets the roles required for the given request path. Returns null when the path is not
     * secured at all.
     *
     * @param requestPath the path of the request relative to the context path.
     * @return the required user roles or null if the path is not secured.
     */
    private List<String> getRequiredRoles(String requestPath) {
        for (Constraint constraint : constraints) {
            if (constraint.getPathSpec().matches(requestPath)) {
                return constraint.getRequiredRoles();
            }
        }

        return null;
    }

    /**
     * Parses the basic credentials of the authorization header. Returns null when the header is
     * missing or malformed.
     *
     * @param authorizationHeader the authorization header of the request.
     * @return the parsed credentials or null.
     */
    private Credentials parseCredentials(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            return null;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(authorizationHeader.substring(BASIC_PREFIX.length()).trim()),
                    StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');

            if (separator < 0) {
                return null;
            }

            return new Credentials(decoded.substring(0, separator), decoded.substring(separator + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Finds the user with the given name.
     *
     * @param username the name of the user.
     * @return the user or null if the user is unknown.
     */
    private User findUser(String username) {
        for (User user : authentication.users()) {
            if (user.getName() != null && user.getName().equals(username)) {
                return user;
            }
        }

        return null;
    }

    /**
     * Compares the given passwords in constant time.
     *
     * @param expectedPassword the password of the user.
     * @param password the password provided by the client.
     * @return true if the passwords match.
     */
    private boolean isPasswordMatching(String expectedPassword, String password) {
        String expected = expectedPassword != null ? expectedPassword : "";
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Checks if the given user has one of the required roles.
     *
     * @param user the authenticated user.
     * @param requiredRoles the roles required to access the resource.
     * @return true if the user is allowed to access the resource.
     */
    private boolean hasRequiredRole(User user, List<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }

        String[] userRoles = user.getRoles();
        if (userRoles == null) {
            return false;
        }

        for (String userRole : userRoles) {
            if (requiredRoles.contains(userRole)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Constraint combining a path spec with the roles required to access the resource.
     */
    private static final class Constraint {

        private final HttpPathSpec pathSpec;
        private final List<String> requiredRoles;

        private Constraint(HttpPathSpec pathSpec, List<String> requiredRoles) {
            this.pathSpec = pathSpec;
            this.requiredRoles = requiredRoles;
        }

        public HttpPathSpec getPathSpec() {
            return pathSpec;
        }

        public List<String> getRequiredRoles() {
            return requiredRoles;
        }

        public int getSpecificity() {
            return pathSpec.getSpecificity();
        }
    }

    /**
     * Credentials of a basic authentication request.
     */
    private static final class Credentials {

        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }
    }
}
