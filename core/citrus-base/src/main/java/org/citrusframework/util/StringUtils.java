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

/**
 * Utility helper class for Strings.
 */
public class StringUtils {

    private StringUtils() {
        //prevent instantiation of utility class
    }

    /**
     * Helper method checks for non-null and non-blank String.
     */
    public static boolean hasText(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * String helper checking for isEmpty String and adds null check on given parameter.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String appendSegmentToPath(String path, String segment) {

        if (path == null) {
            return segment;
        }

        if (segment == null) {
            return path;
        }

        if (!path.endsWith("/")) {
            path = path +"/";
        }

        if (segment.startsWith("/")) {
            segment = segment.substring(1);
        }

        return path+segment;
    }
}
