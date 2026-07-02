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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Appends a URL segment to an existing URL path.
     * <p>
     * This method ensures that the provided segment is appended correctly to the base path,
     * handling cases where there may or may not be existing separators (`/`). It avoids duplicate
     * separators and ensures proper formatting of the resulting URL.
     * </p>
     *
     * @param path    the base URL path to which the segment will be appended. If {@code null}, the segment will be returned.
     * @param segment the segment to append to the base path. If {@code null}, the original path will be returned.
     * @return the combined URL path with the appended segment, properly formatted.
     */
    public static String appendSegmentToUrlPath(String path, String segment) {
        if (path == null) {
            return segment;
        }

        if (isEmpty(segment)) {
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
    public static String convertFirstCharToUpperCase(String input) {
        if (input != null && !input.isEmpty()) {
            String firstLetter = input.substring(0, 1).toUpperCase(Locale.ROOT);
            return input.length() == 1 ? firstLetter : firstLetter + input.substring(1);
        } else {
            return "";
        }
    }

    /**
     * Converts the first letter of the given input string to lowercase while leaving
     * the rest of the string unchanged. If the input string is empty or null,
     * an empty string is returned.
     *
     * @param input The string to be converted. It can be null or empty.
     * @return the string in with upper case first letter
     */
    public static String convertFirstCharToLowerCase(String input) {
        if (input != null && !input.isEmpty()) {
            String firstLetter = input.substring(0, 1).toLowerCase(Locale.ROOT);
            return input.length() == 1 ? firstLetter : firstLetter + input.substring(1);
        } else {
            return "";
        }
    }

    /**
     * Normalizes the given text by replacing all whitespace characters (identified by {@link Character#isWhitespace) by a single space
     * and replacing windows style line endings with unix style line endings.
     */
    public static String normalizeWhitespace(String text, boolean normalizeWhitespace, boolean normalizeLineEndingsToUnix) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (normalizeWhitespace) {
            StringBuilder result = new StringBuilder();
            boolean lastWasSpace = true;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isWhitespace(c)) {
                    if (!lastWasSpace) {
                        result.append(' ');
                    }
                    lastWasSpace = true;
                } else {
                    result.append(c);
                    lastWasSpace = false;
                }
            }
            return result.toString().trim();
        }

        if (normalizeLineEndingsToUnix) {
            return text.replaceAll("\\r(\\n)?", "\n");
        }

        return text;
    }

    /**
     * Converts given map to a Json style String representation.
     * Handles null values and numeric values with Json style value representation.
     * Handles keys and String values with quotes.
     */
    public static String convertToString(Map<String, Object> map) {
        return map.entrySet()
                .stream()
                .map(entry -> {
                    if (entry.getValue() == null) {
                        return "\"%s\": null".formatted(entry.getKey());
                    } else if (ObjectHelper.isNumeric(entry.getValue())) {
                        return "\"%s\": %s".formatted(entry.getKey(), entry.getValue());
                    }

                    return "\"%s\": \"%s\"".formatted(entry.getKey(), entry.getValue());
                })
                .collect(Collectors.joining(", ", "{", "}"));
    }

    /**
     * Extract map from given String key value pairs.
     * Supports Json style object with single level properties.
     * Supports comma-delimited list of key value pairs with format "key=value".
     */
    public static Map<String, Object> extractMap(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        if (value.trim().startsWith("{") && value.trim().endsWith("}")) {
            // Parse Json style value
            String json = value.trim().substring(1, value.trim().length() - 1);
            String[] properties = json.split(",");
            for (String property : properties) {
                String[] tokens = property.trim().split(":", 2);
                if (tokens.length == 2) {
                    result.put(stripQuotes(tokens[0].trim()), stripQuotes(tokens[1].trim()));
                }
            }
            return result;
        } else {
            // assume comma-delimited key value paris
            String[] keyValuePairs = value.trim().split(",");
            for (String pair : keyValuePairs) {
                String[] tokens = pair.trim().split("=", 2);
                if (tokens.length == 2) {
                    result.put(stripQuotes(tokens[0].trim()), stripQuotes(tokens[1].trim()));
                }
            }
        }

        return result;
    }

    /**
     * Remove quotes from given value if any.
     */
    public static String stripQuotes(String value) {
        if (value != null && value.trim().startsWith("\"") && value.trim().endsWith("\"")) {
            return value.trim().substring(1, value.trim().length() - 1);
        }

        return value;
    }
}
