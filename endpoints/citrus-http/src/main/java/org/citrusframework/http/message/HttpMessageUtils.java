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

package org.citrusframework.http.message;

import static org.citrusframework.http.message.HttpMessageHeaders.HTTP_QUERY_PARAMS;
import static org.citrusframework.util.StringUtils.hasText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;

/**
 * @since 2.7.5
 */
public final class HttpMessageUtils {

    /**
     * Prevent instantiation.
     */
    private HttpMessageUtils() {
        super();
    }

    /**
     * Apply message settings to target http message.
     * @param from
     * @param to
     */
    public static void copy(Message from, HttpMessage to) {
        HttpMessage source;
        if (from instanceof HttpMessage httpMessage) {
            source = httpMessage;
        } else {
            source = new HttpMessage(from);
        }

        copy(source, to);
    }

    /**
     * Apply message settings to target http message.
     * @param from
     * @param to
     */
    public static void copy(HttpMessage from, HttpMessage to) {
        to.setName(from.getName());
        to.setType(from.getType());
        to.setPayload(from.getPayload());

        from.getHeaders().entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equals(MessageHeaders.ID) && !entry.getKey().equals(MessageHeaders.TIMESTAMP))
                .forEach(entry -> to.header(entry.getKey(), entry.getValue()));

        from.getHeaderData().forEach(to::addHeaderData);
        from.getCookies().forEach(to::cookie);
    }

    /**
     * Extracts query parameters from the citrus HTTP message header and returns them as a map.
     *
     * @param httpMessage the HTTP message containing the query parameters in the header
     * @return a map of query parameter names and their corresponding values
     * @throws IllegalArgumentException if the query parameters are not formatted correctly
     */
    public static Map<String, List<String>> getQueryParameterMap(HttpMessage httpMessage) {
        String queryParams = (String) httpMessage.getHeader(HTTP_QUERY_PARAMS);
        if (hasText(queryParams)) {
            return Arrays.stream(queryParams.split(","))
                .map(queryParameterKeyValue -> {
                    String[] keyAndValue = queryParameterKeyValue.split("=", 2);
                    if (keyAndValue.length == 0) {
                        throw new IllegalArgumentException("Query parameter must have a key.");
                    }
                    String key = keyAndValue[0];
                    String value = keyAndValue.length > 1 ? keyAndValue[1] : "";
                    return Pair.of(key, value);
                })
                .collect(Collectors.groupingBy(
                    Pair::getLeft, Collectors.mapping(Pair::getRight, Collectors.toList())));
        }
        return Collections.emptyMap();
    }
}
