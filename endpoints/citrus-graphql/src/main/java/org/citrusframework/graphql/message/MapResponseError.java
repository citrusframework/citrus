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
package org.citrusframework.graphql.message;

import java.util.List;
import java.util.Map;

import graphql.ErrorClassification;
import graphql.language.SourceLocation;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.execution.ErrorType;

/**
 * {@link ResponseError} backed by one entry of a response's {@code errors} array.
 */
public class MapResponseError implements ResponseError {

    private final Map<String, Object> errorMap;

    public MapResponseError(Map<String, Object> errorMap) {
        this.errorMap = errorMap;
    }

    @Override
    public String getMessage() {
        return errorMap.get("message") instanceof String message ? message : null;
    }

    /**
     * Classification from {@code extensions.classification}: a graphql-java or Spring GraphQL error
     * type when the name matches one, otherwise a classification with that name; {@code null} when absent.
     */
    @Override
    public ErrorClassification getErrorType() {
        if (!(getExtensions().get("classification") instanceof String classification)) {
            return null;
        }

        for (graphql.ErrorType type : graphql.ErrorType.values()) {
            if (type.name().equals(classification)) {
                return type;
            }
        }

        for (ErrorType type : ErrorType.values()) {
            if (type.name().equals(classification)) {
                return type;
            }
        }

        return ErrorClassification.errorClassification(classification);
    }

    /**
     * The path as a string, e.g. {@code books[1].title}; empty when the error has no path.
     */
    @Override
    public String getPath() {
        StringBuilder path = new StringBuilder();
        for (Object segment : getParsedPath()) {
            if (segment instanceof Number index) {
                path.append('[').append(index).append(']');
            } else {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(segment);
            }
        }

        return path.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> getParsedPath() {
        return errorMap.get("path") instanceof List<?> path ? (List<Object>) path : List.of();
    }

    @Override
    public List<SourceLocation> getLocations() {
        if (!(errorMap.get("locations") instanceof List<?> locations)) {
            return List.of();
        }

        return locations.stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(location -> new SourceLocation(toInt(location.get("line")), toInt(location.get("column"))))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getExtensions() {
        return errorMap.get("extensions") instanceof Map<?, ?> extensions ? (Map<String, Object>) extensions : Map.of();
    }

    @Override
    public String toString() {
        return errorMap.toString();
    }

    private static int toInt(Object value) {
        return value instanceof Number number ? number.intValue() : -1;
    }
}
