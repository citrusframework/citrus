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
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/** Immutable, single-user Basic authentication for the test-only HTTP prototype. */
final class VertxBasicAuthentication {
    private final byte[] credentials;
    private final String challenge;

    VertxBasicAuthentication(String username, String password, String realm) {
        requireAscii(username);
        requireAscii(password);
        requireAscii(realm);
        if (username.isEmpty() || username.contains(":")) {
            throw new IllegalArgumentException("Basic username must be nonempty and contain no colon");
        }
        credentials = (username + ":" + password).getBytes(StandardCharsets.US_ASCII);
        challenge = "Basic realm=\"" + realm.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    String challenge() { return challenge; }

    boolean accepts(List<String> authorization) {
        if (authorization.size() != 1) return false;
        String value = authorization.get(0);
        int space = value.indexOf(' ');
        if (space < 0 || !value.substring(0, space).equalsIgnoreCase("Basic")) return false;
        int tokenStart = space;
        while (tokenStart < value.length() && value.charAt(tokenStart) == ' ') tokenStart++;
        try {
            byte[] supplied = Base64.getDecoder().decode(value.substring(tokenStart));
            return MessageDigest.isEqual(credentials, supplied);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void requireAscii(String value) {
        Objects.requireNonNull(value, "Basic authentication fields must not be null");
        if (value.chars().anyMatch(c -> c < 32 || c > 126)) {
            throw new IllegalArgumentException("Basic authentication fields must contain printable ASCII only");
        }
    }
}
