/*
 * Copyright 2020 the original author or authors.
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

package org.citrusframework.util;

import java.util.Optional;

import org.citrusframework.message.Message;

/**
 * @author Christoph Deppisch
 */
public class MessageUtils {

    /**
     * Prevent instantiation of utility class.
     */
    private MessageUtils() {
        // prevent instantiation
    }

    /**
     * Checks if given message payload is of type XML. An empty payload is considered to be a valid Json payload.
     * @param message to check.
     * @return true if message payload is XML, false otherwise.
     */
    public static boolean hasXmlPayload(Message message) {
        if (!(message.getPayload() instanceof String)) {
            return false;
        }

        return Optional.ofNullable(message.getPayload(String.class))
                .map(String::trim)
                .map(payload -> IsXmlPredicate.getInstance().test(payload))
                .orElse(true);
    }

    /**
     * Checks if message payload is of type Json. An empty payload is considered to be a valid Json payload.
     * @param message to check.
     * @return true if payload is Json, false otherwise.
     */
    public static boolean hasJsonPayload(Message message) {
        if (!(message.getPayload() instanceof String)) {
            return false;
        }

        return Optional.ofNullable(message.getPayload(String.class))
                .map(String::trim)
                .map(payload->IsJsonPredicate.getInstance().test(payload))
                .orElse(true);
    }

}
