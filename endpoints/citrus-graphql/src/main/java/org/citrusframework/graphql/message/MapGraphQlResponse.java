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

import org.springframework.graphql.ResponseError;
import org.springframework.graphql.support.AbstractGraphQlResponse;

/**
 * {@link org.springframework.graphql.GraphQlResponse} backed by the deserialized response map.
 * Spring GraphQL's own map-backed implementation is not public.
 */
public class MapGraphQlResponse extends AbstractGraphQlResponse {

    private final Map<String, Object> responseMap;
    private final List<ResponseError> errors;

    @SuppressWarnings("unchecked")
    public MapGraphQlResponse(Map<String, Object> responseMap) {
        this.responseMap = responseMap;
        this.errors = responseMap.get("errors") instanceof List<?> list
                ? list.stream()
                    .map(error -> (ResponseError) new MapResponseError(error instanceof Map<?, ?> map
                            ? (Map<String, Object>) map
                            : Map.of("message", String.valueOf(error))))
                    .toList()
                : List.of();
    }

    @Override
    public boolean isValid() {
        return responseMap.get("data") != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) responseMap.get("data");
    }

    @Override
    public List<ResponseError> getErrors() {
        return errors;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, Object> getExtensions() {
        return responseMap.get("extensions") instanceof Map<?, ?> extensions ? (Map<Object, Object>) extensions : Map.of();
    }

    @Override
    public Map<String, Object> toMap() {
        return responseMap;
    }

    @Override
    public String toString() {
        return responseMap.toString();
    }
}
