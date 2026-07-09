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

package org.citrusframework.log;

import java.util.concurrent.atomic.AtomicBoolean;

public final class LogColors {

    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";
    public static final String GREEN = "\033[32m";
    public static final String RED = "\033[31m";
    public static final String YELLOW = "\033[33m";
    public static final String CYAN = "\033[36m";

    private static final AtomicBoolean colorEnabled = new AtomicBoolean(resolveColorEnabled());

    private LogColors() {
    }

    public static void setColorEnabled(boolean enabled) {
        colorEnabled.set(enabled);
    }

    public static boolean isColorEnabled() {
        return colorEnabled.get();
    }

    private static boolean resolveColorEnabled() {
        String mode = CitrusLogSettings.getColorMode();
        return switch (mode.toLowerCase()) {
            case "always" -> true;
            case "never" -> false;
            case "auto" -> detectTerminalColor();
            default -> throw new IllegalStateException("Unknown log color mode: " + mode);
        };
    }

    private static boolean detectTerminalColor() {
        String term = System.getenv("TERM");
        return !"dumb".equals(term) || System.console() != null;
    }

    public static String colorize(String text, String ansiCode) {
        if (isColorEnabled()) {
            return ansiCode + text + RESET;
        }
        return text;
    }

    public static String success(String text) {
        return colorize(text, GREEN);
    }

    public static String failed(String text) {
        return colorize(text, RED);
    }

    public static String skipped(String text) {
        return colorize(text, YELLOW);
    }

    public static String arrow(String text) {
        return colorize(text, CYAN);
    }

    public static String start(String text) {
        return colorize(text, CYAN);
    }

    public static String dim(String text) {
        return colorize(text, DIM);
    }

    public static String bold(String text) {
        return colorize(text, BOLD);
    }

    public static void resetColorsEnabled() {
        colorEnabled.set(resolveColorEnabled());
    }
}
