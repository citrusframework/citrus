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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.util.Arrays;

/**
 * Custom JsonPath function support for size(), keySet() and toString() operations on Json objects and arrays.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class JsonPathFunctions {

    public static final String DEFAULT_FUNCTION = "toString";

    private static final String[] FUNCTION_NAMES = {"keySet", "size", DEFAULT_FUNCTION};

    /**
     * Evaluates function on result. Supported functions are size(), keySet() and toString().
     * @param jsonPathResult
     * @param jsonPathFunction
     * @return
     */
    public static String evaluate(Object jsonPathResult, String jsonPathFunction) {
        if (jsonPathFunction.equals("size")) {
            if (jsonPathResult instanceof JSONArray) {
                return String.valueOf(((JSONArray) jsonPathResult).size());
            } else if (jsonPathResult instanceof JSONObject) {
                return String.valueOf(((JSONObject) jsonPathResult).size());
            } else {
                return "0";
            }
        } else if (jsonPathFunction.equals("keySet")) {
            if (jsonPathResult instanceof JSONObject) {
                StringBuilder keySetString = new StringBuilder();

                for (String key :((JSONObject) jsonPathResult).keySet())  {
                    keySetString.append(key + ",");
                }

                if (keySetString.length() > 0) {
                    return String.format("[%s]", keySetString.toString().substring(0, keySetString.length() - 1));
                } else {
                    return "[]";
                }
            } else {
                return "[]";
            }
        } else if (jsonPathFunction.equals("toString")) {
            if (jsonPathResult instanceof JSONArray) {
                return ((JSONArray) jsonPathResult).toJSONString();
            } else {
                return jsonPathResult.toString();
            }
        }

        throw new CitrusRuntimeException("Unsupported JsonPath function: " + jsonPathFunction);
    }

    /**
     * Gets names of supported functions.
     * @return
     */
    public static String[] getSuportedFunctions() {
        return Arrays.copyOf(FUNCTION_NAMES, FUNCTION_NAMES.length);
    }
}
