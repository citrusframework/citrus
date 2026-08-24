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

package org.citrusframework.playwright.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Redacts obvious and user-configured secret values from diagnostics.
 */
public class SecretPatternRedactor {

    public static final String MASK = "***";

    private static final List<String> DEFAULT_PATTERNS = List.of(
            "authorization", "x-api-key", "api-key", "password", "token", "api_key",
            "apikey", "secret", "auth", "cookie");

    private final List<String> patterns;
    private final List<Pattern> assignmentPatterns;
    private final List<Pattern> literalValuePatterns;

    /**
     * Creates a redactor with built-in patterns only.
     */
    public SecretPatternRedactor() {
        this(List.of());
    }

    /**
     * Creates a redactor with built-in and additional name patterns.
     *
     * @param additionalPatterns extra case-insensitive name fragments
     */
    public SecretPatternRedactor(Collection<String> additionalPatterns) {
        List<String> values = new ArrayList<>(DEFAULT_PATTERNS);
        List<String> literals = new ArrayList<>();
        if (additionalPatterns != null) {
            additionalPatterns.stream()
                    .filter(pattern -> pattern != null && !pattern.isBlank())
                    .map(pattern -> pattern.toLowerCase(Locale.ROOT))
                    .forEach(pattern -> {
                        values.add(pattern);
                        literals.add(pattern);
                    });
        }
        this.patterns = List.copyOf(values);
        this.assignmentPatterns = values.stream()
                .map(SecretPatternRedactor::assignmentPattern)
                .toList();
        this.literalValuePatterns = literals.stream()
                .map(pattern -> Pattern.compile(Pattern.quote(pattern), Pattern.CASE_INSENSITIVE))
                .toList();
    }

    /**
     * Redacts secret query parameter values from a URL.
     *
     * @param value URL value
     * @return sanitized URL
     */
    public String sanitizeUrl(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int queryStart = value.indexOf('?');
        if (queryStart < 0) {
            return value;
        }
        int fragmentStart = value.indexOf('#', queryStart);
        String prefix = value.substring(0, queryStart + 1);
        String query = fragmentStart < 0 ? value.substring(queryStart + 1) : value.substring(queryStart + 1, fragmentStart);
        String fragment = fragmentStart < 0 ? "" : value.substring(fragmentStart);
        return prefix + sanitizeQuery(query) + fragment;
    }

    /**
     * Redacts secret header values.
     *
     * @param source source headers
     * @return sanitized headers in source iteration order
     */
    public Map<String, String> sanitizeHeaders(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, String> sanitized = new LinkedHashMap<>();
        source.forEach((name, value) -> sanitized.put(name, isSecretName(name) ? maskHeaderValue(value) : value));
        return Collections.unmodifiableMap(sanitized);
    }

    /**
     * Redacts simple name=value pairs embedded in diagnostic text.
     *
     * @param value diagnostic text
     * @return sanitized text
     */
    public String sanitizeText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String sanitized = value;
        for (Pattern regex : assignmentPatterns) {
            sanitized = regex.matcher(sanitized).replaceAll("$1" + MASK);
        }
        for (Pattern pattern : literalValuePatterns) {
            sanitized = pattern.matcher(sanitized).replaceAll(MASK);
        }
        return sanitized;
    }

    /**
     * Redacts a value when its name matches a secret pattern.
     *
     * @param name value name
     * @param value value
     * @return redacted or original value
     */
    public String sanitizeNamedValue(String name, String value) {
        return isSecretName(name) ? MASK : sanitizeText(value);
    }

    /**
     * Reports whether a name matches a secret pattern.
     *
     * @param name header, query, cookie, storage, or parameter name
     * @return true when the name should be redacted
     */
    public boolean isSecretName(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return patterns.stream().anyMatch(normalized::contains);
    }

    private String sanitizeQuery(String query) {
        String[] pairs = query.split("&", -1);
        for (int i = 0; i < pairs.length; i++) {
            int separator = pairs[i].indexOf('=');
            String name = separator < 0 ? pairs[i] : pairs[i].substring(0, separator);
            if (isSecretName(name)) {
                pairs[i] = name + "=" + MASK;
            }
        }
        return String.join("&", pairs);
    }

    private String maskHeaderValue(String value) {
        if (value != null && value.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return "Bearer " + MASK;
        }
        return MASK;
    }

    private static Pattern assignmentPattern(String pattern) {
        return Pattern.compile("([A-Za-z0-9_.-]*" + Pattern.quote(pattern)
                + "[A-Za-z0-9_.-]*\\s*=)\\s*([^;&\\s]+)", Pattern.CASE_INSENSITIVE);
    }
}
