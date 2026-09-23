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

package org.citrusframework.playwright.util;

import java.util.Arrays;
import java.util.Locale;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Shared helpers for the XML and YAML Playwright action wrappers.
 */
public final class DslCommands {

    private DslCommands() {
    }

    /**
     * Normalizes a declarative command or option value: trimmed, lower case, underscores as dashes.
     *
     * @param value raw value, may be {@code null}
     * @return normalized value, empty when the value is {@code null}
     */
    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    /**
     * Splits a declarative list attribute on commas. Each entry is trimmed and blank entries are
     * dropped, so values may contain spaces.
     *
     * @param values raw attribute value, may be {@code null}
     * @return individual values, empty when the value is {@code null} or blank
     */
    public static String[] split(String values) {
        if (values == null || values.isBlank()) {
            return new String[0];
        }
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
    }

    /**
     * Resolves a declarative enum option value, failing with a Playwright scoped message.
     *
     * @param type enum type
     * @param option option name used in the error message
     * @param value option value as given in the test
     * @return matching enum constant
     */
    public static <T extends Enum<T>> T option(Class<T> type, String option, String value) {
        try {
            return Enum.valueOf(type, normalize(value).replace('-', '_').toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright %s: %s".formatted(option, value), e);
        }
    }

    /**
     * Creates the error raised for a missing or unknown declarative command.
     *
     * @param action declarative action name
     * @param command command value as given in the test
     * @return exception to throw
     */
    public static CitrusRuntimeException unsupported(String action, String command) {
        if (command == null || command.isBlank()) {
            return new CitrusRuntimeException("Missing Playwright %s command".formatted(action));
        }
        return new CitrusRuntimeException("Unsupported Playwright %s command: %s".formatted(action, command));
    }

    /**
     * Loads a type given by its fully qualified class name and checks it against the expected type.
     *
     * @param type fully qualified class name
     * @param expected type the loaded class must be assignable to
     * @return loaded class
     */
    public static Class<?> loadClass(String type, Class<?> expected) {
        if (type == null || type.isBlank()) {
            throw new CitrusRuntimeException("Missing Playwright type name");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = DslCommands.class.getClassLoader();
        }

        Class<?> loaded;
        try {
            loaded = Class.forName(type.trim(), false, classLoader);
        } catch (ClassNotFoundException e) {
            throw new CitrusRuntimeException("Unable to load Playwright type: " + type, e);
        }

        if (!expected.isAssignableFrom(loaded)) {
            throw new CitrusRuntimeException("Playwright type %s is not a %s".formatted(type, expected.getName()));
        }
        return loaded;
    }
}
