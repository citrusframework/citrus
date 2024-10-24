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

import java.util.Locale;

/**
 * Utility helper class for Strings.
 */
public class StringUtils {

    public static final String URL_PATH_SEPARATOR = "/";

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
     * Helper method checks for null or blank String.
     * @param str
     * @return
     */
    public static boolean hasNoText(String str) {
        return !hasText(str);
    }

    /**
     * String helper checking for isEmpty String and adds null check on given parameter.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * String helper checking for isEmpty String and adds null check on given parameter.
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String appendSegmentToUrlPath(String path, String segment) {
        if (path == null) {
            return segment;
        }

        if (segment == null) {
            return path;
        }

        if (!path.endsWith(URL_PATH_SEPARATOR)) {
            path = path + URL_PATH_SEPARATOR;
        }

        if (segment.startsWith(URL_PATH_SEPARATOR)) {
            segment = segment.substring(1);
        }

        return path + segment;
    }

    public static String quote(String text, boolean quote) {
        return quote ? "\"" + text + "\"" : text;
    }

    /**
     * Trims trailing whitespace characters and the first trailing comma from the end of the given StringBuilder.
     * <p>
     * This method removes all trailing whitespace characters (such as spaces, tabs, and newline characters)
     * and the first trailing comma found from the end of the content in the provided StringBuilder.
     * Any additional commas or whitespace characters after the first trailing comma are not removed.
     *
     * @param builder the StringBuilder whose trailing whitespace characters and first comma are to be removed
     */
    public static void trimTrailingComma(StringBuilder builder) {
        int length = builder.length();
        while (length > 0 && (builder.charAt(length - 1) == ',' || Character.isWhitespace(builder.charAt(length - 1)))) {
            char c = builder.charAt(length - 1);
            builder.deleteCharAt(length - 1);

            if (c == ',') {
                return;
            }

            length = builder.length();
        }
    }

    /**
     * Converts the first letter of the given input string to uppercase while leaving
     * the rest of the string unchanged. If the input string is empty or null,
     * an empty string is returned.
     *
     * @param input The string to be converted. It can be null or empty.
     * @return the string in with upper case first letter
     */
    public static String convertFirstChartToUpperCase(String input) {
        if (input != null && !input.isEmpty()) {
            String firstLetter = input.substring(0, 1).toUpperCase(Locale.ROOT);
            return input.length() == 1 ? firstLetter : firstLetter + input.substring(1);
        } else {
            return "";
        }
    }
}
