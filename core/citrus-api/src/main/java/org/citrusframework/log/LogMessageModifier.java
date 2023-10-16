/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.log;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.citrusframework.CitrusSettings;
import org.citrusframework.message.Message;

/**
 * Special modifier adds message related modifications on logger output on headers and body.
 *
 * @author Christoph Deppisch
 */
@FunctionalInterface
public interface LogMessageModifier extends LogModifier {

    /**
     * Mask the given message body to not print sensitive data.
     * @param message
     * @return
     */
    default String maskBody(Message message) {
        return mask(message.getPayload(String.class).trim());
    }

    /**
     * Mask the given message header values to not print sensitive data.
     * @param message
     * @return
     */
    default Map<String, Object> maskHeaders(Message message) {
        return message.getHeaders().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (entry.getValue() == null) {
                        return "";
                    }

                    String keyValuePair = String.format("%s=%s", entry.getKey(), entry.getValue());
                    if (!keyValuePair.equals(mask(keyValuePair))) {
                        return CitrusSettings.getLogMaskValue();
                    }

                    return entry.getValue();
                },
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new));
    }
    /**
     * Mask the given message header data to not print sensitive data.
     * @param message
     * @return
     */
    default List<String> maskHeaderData(Message message) {
        if (message.getHeaderData() == null || message.getHeaderData().isEmpty()) {
            return Collections.emptyList();
        }

        return message.getHeaderData()
                .stream()
                .map(this::mask)
                .collect(Collectors.toList());
    }
}
