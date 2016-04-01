/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.validation.json;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;

/**
 * Custom JsonPath function support for size(), keySet() and toString() operations on Json objects and arrays.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JsonPathFunctions {

    private static final String[] FUNCTION_NAMES = {"keySet", "size", "values", "toString"};

    /**
     * Evaluates function on result. Supported functions are size(), keySet(), values() and toString().
     * @param jsonPathResult
     * @param jsonPathFunction
     * @return
     */
    public static Object evaluate(Object jsonPathResult, String jsonPathFunction) {
        if (jsonPathFunction.equals("size")) {
            if (jsonPathResult instanceof JSONArray) {
                return ((JSONArray) jsonPathResult).size();
            } else if (jsonPathResult instanceof JSONObject) {
                return ((JSONObject) jsonPathResult).size();
            } else {
                return 0;
            }
        } else if (jsonPathFunction.equals("keySet")) {
            if (jsonPathResult instanceof JSONObject) {
                return ((JSONObject) jsonPathResult).keySet();
            } else {
                return Collections.emptySet();
            }
        } else if (jsonPathFunction.equals("values")) {
            if (jsonPathResult instanceof JSONObject) {
                return ((JSONObject) jsonPathResult).values().toArray();
            } else {
                return new Object[] {};
            }
        }

        return jsonPathResult.toString();
    }

    /**
     * Gets names of supported functions.
     * @return
     */
    public static String[] getSupportedFunctions() {
        return Arrays.copyOf(FUNCTION_NAMES, FUNCTION_NAMES.length);
    }
}
