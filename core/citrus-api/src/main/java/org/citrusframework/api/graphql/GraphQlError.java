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
package org.citrusframework.api.graphql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A GraphQL error as used by the GraphQL test actions: as an expectation when receiving a response
 * (every field that is set must match, fields may hold validation matcher expressions) and as an entry
 * of the {@code errors} array when a simulator replies.
 */
public final class GraphQlError {

    private static final Pattern PATH_SEGMENT = Pattern.compile("([^.\\[\\]]+)|\\[(\\d+)]");

    private String message;
    private final List<Object> path = new ArrayList<>();
    private String code;

    private GraphQlError() {
    }

    /**
     * Starts a new error; set the fields to expect or to reply with.
     */
    public static GraphQlError error() {
        return new GraphQlError();
    }

    public GraphQlError message(String message) {
        this.message = message;
        return this;
    }

    /**
     * Sets the path as segments: field names as strings, list indexes as integers.
     */
    public GraphQlError path(Object... segments) {
        this.path.clear();
        Collections.addAll(this.path, segments);
        return this;
    }

    /**
     * Sets {@code extensions.code}.
     */
    public GraphQlError code(String code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public List<Object> getPath() {
        return Collections.unmodifiableList(path);
    }

    public String getCode() {
        return code;
    }

    /**
     * Parses a path in its string form, e.g. {@code books[1].title}, into segments.
     */
    public static List<Object> parsePath(String path) {
        List<Object> segments = new ArrayList<>();
        if (path == null) {
            return segments;
        }

        Matcher matcher = PATH_SEGMENT.matcher(path);
        while (matcher.find()) {
            segments.add(matcher.group(1) != null ? matcher.group(1) : Integer.valueOf(matcher.group(2)));
        }

        return segments;
    }

    /**
     * Formats path segments in their string form, e.g. {@code books[1].title}.
     */
    public static String formatPath(List<?> segments) {
        StringBuilder formatted = new StringBuilder();
        for (Object segment : segments) {
            if (segment instanceof Number index) {
                formatted.append('[').append(index).append(']');
            } else {
                if (!formatted.isEmpty()) {
                    formatted.append('.');
                }
                formatted.append(segment);
            }
        }

        return formatted.toString();
    }

    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        if (code != null) {
            parts.add("[%s]".formatted(code));
        }
        if (message != null) {
            parts.add(message);
        }
        if (!path.isEmpty()) {
            parts.add("at " + formatPath(path));
        }

        return parts.isEmpty() ? "<any error>" : String.join(" ", parts);
    }
}
