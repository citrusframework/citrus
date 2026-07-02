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

package org.citrusframework.util;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Helper adds utility operations such as non-null assertion on objects.
 */
public class ObjectHelper {

    private ObjectHelper() {
        //prevent instantiation of utility class
    }

    public static <T> T assertNotNull(T object) {
        return assertNotNull(object, "Provided object must not be null");
    }

    public static <T> T assertNotNull(T object, String errorMessage) {
        if (object == null) {
            throw new CitrusRuntimeException(errorMessage);
        }

        return object;
    }

    /**
     * Check if given Object is either a primitive number or an instance of java.lang.Number.
     * For String values check if given String can be parsed as a number.
     */
    public static boolean isNumeric(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Number) {
            return true;
        }

        if (value instanceof String stringValue) {
            try {
                Double.parseDouble(stringValue);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return false;
    }
}
