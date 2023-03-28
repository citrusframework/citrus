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

package org.citrusframework.json;

import net.minidev.json.parser.JSONParser;

/**
 * @author Christoph Deppisch
 */
public final class JsonSettings {

    private static final String MESSAGE_VALIDATION_STRICT_PROPERTY = "citrus.json.message.validation.strict";
    private static final String MESSAGE_VALIDATION_STRICT_ENV = "CITRUS_JSON_MESSAGE_VALIDATION_STRICT";
    private static final String MESSAGE_VALIDATION_STRICT_DEFAULT = "true";

    private static final String PERMISSIVE_MODE_PROPERTY = "citrus.json.permissive.mode";
    private static final String PERMISSIVE_MODE_ENV = "CITRUS_JSON_PERMISSIVE_MODE";
    private static final String PERMISSIVE_MODE_DEFAULT = String.valueOf(JSONParser.MODE_JSON_SIMPLE);

    /**
     * Private constructor prevent instantiation of utility class
     */
    private JsonSettings() {
        // prevent instantiation
    }

    /**
     * Gets the permissive mode set on the Json parser.
     * @return
     */
    public static int getPermissiveMoe() {
        String mode = System.getProperty(PERMISSIVE_MODE_PROPERTY, System.getenv(PERMISSIVE_MODE_ENV) != null ?
                        System.getenv(PERMISSIVE_MODE_ENV) : PERMISSIVE_MODE_DEFAULT);

        switch (mode) {
            case "SIMPLE":
                return JSONParser.MODE_JSON_SIMPLE;
            case "RFC4627":
                return JSONParser.MODE_RFC4627;
            case "STRICTEST":
                return JSONParser.MODE_STRICTEST;
            case "PERMISSIVE":
                return JSONParser.MODE_PERMISSIVE;
            default:
                return Integer.parseInt(mode);
        }
    }

    /**
     * Is Json message validation strict
     * @return
     */
    public static boolean isStrict() {
        return Boolean.parseBoolean(
                System.getProperty(MESSAGE_VALIDATION_STRICT_PROPERTY, System.getenv(MESSAGE_VALIDATION_STRICT_ENV) != null ?
                        System.getenv(MESSAGE_VALIDATION_STRICT_ENV) : MESSAGE_VALIDATION_STRICT_DEFAULT));
    }
}
