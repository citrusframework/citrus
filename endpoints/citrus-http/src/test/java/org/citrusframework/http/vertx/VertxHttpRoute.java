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
import java.util.Locale;
import java.util.Objects;

import org.springframework.web.util.UriUtils;
import org.springframework.web.util.UrlPathHelper;

/** One immutable context and dispatcher URL mapping; does not host Java Servlets. */
final class VertxHttpRoute {
    private final String context;
    private final String mapping;

    VertxHttpRoute(String context, String mapping) {
        Objects.requireNonNull(context, "contextPath");
        this.context = "/".equals(context) ? "" : context;
        this.mapping = Objects.requireNonNull(mapping, "servletMappingPath");
        if (!this.context.isEmpty() && !this.context.matches("(/[A-Za-z0-9_-]+)+")) {
            throw new IllegalArgumentException("Context must contain plain path segments, without a trailing slash");
        }
        boolean extension = mapping.matches("\\*\\.[A-Za-z0-9_-]+");
        String stem = mapping.endsWith("/*") ? mapping.substring(0, mapping.length() - 2) : mapping;
        if (!mapping.isEmpty() && !extension && !(mapping.startsWith("/")
                && !stem.contains("*") && !stem.contains("?") && !stem.contains("#")
                && !stem.contains("%") && !stem.contains(";") && !stem.contains("\\")
                && !stem.contains("//") && !hasDotSegment(stem)
                && stem.chars().noneMatch(Character::isISOControl))) {
            throw new IllegalArgumentException("Expected an exact, prefix, extension, default or empty URL mapping");
        }
    }

    String contextPath() { return context; }

    /** Returns 200 for a match, 301 for a missing context slash, 404 or 400 otherwise. */
    int status(String rawPath) {
        String lower = rawPath.toLowerCase(Locale.ROOT);
        if (lower.contains("%2f") || lower.contains("%5c") || lower.contains("%3b")) return 400;
        final String path;
        try {
            path = UriUtils.decode(new UrlPathHelper().removeSemicolonContent(rawPath), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return 400;
        }
        if (!path.startsWith("/") || path.contains("\\") || hasDotSegment(path)
                || path.chars().anyMatch(Character::isISOControl)) return 400;
        if (!context.isEmpty() && path.equals(context)) return 301;
        if (!context.isEmpty() && !path.startsWith(context + "/")) return 404;
        String relative = path.substring(context.length());
        if (mapping.equals("/") || mapping.equals("/*")) return 200;
        if (mapping.isEmpty()) return relative.equals("/") ? 200 : 404;
        if (mapping.startsWith("*.")) {
            return relative.substring(relative.lastIndexOf('/') + 1).endsWith(mapping.substring(1)) ? 200 : 404;
        }
        if (mapping.endsWith("/*")) {
            String prefix = mapping.substring(0, mapping.length() - 2);
            return relative.equals(prefix) || relative.startsWith(prefix + "/") ? 200 : 404;
        }
        return relative.equals(mapping) ? 200 : 404;
    }

    private static boolean hasDotSegment(String path) {
        for (String segment : path.split("/", -1)) {
            if (segment.equals(".") || segment.equals("..")) return true;
        }
        return false;
    }
}
