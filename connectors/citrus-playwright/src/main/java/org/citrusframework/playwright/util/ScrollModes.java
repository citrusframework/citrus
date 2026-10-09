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

import java.util.Locale;

import com.microsoft.playwright.options.ScrollMode;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Resolves the DSL scroll mode names onto the Playwright {@link ScrollMode} enum.
 */
public final class ScrollModes {

    private ScrollModes() {
    }

    /**
     * Parses a scroll mode name as used in the Java, XML and YAML DSLs.
     *
     * @param mode scroll mode name, {@code auto} or {@code none}
     * @return matching scroll mode, {@link ScrollMode#AUTO} when no value is given
     */
    public static ScrollMode parse(String mode) {
        if (mode == null || mode.isBlank()) {
            return ScrollMode.AUTO;
        }

        try {
            return ScrollMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright scroll mode: " + mode, e);
        }
    }
}
