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

import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;

import org.citrusframework.exceptions.CitrusRuntimeException;

/**
 * Resolves the DSL HAR recording option names onto the Playwright enums.
 */
public final class HarPolicies {

    private HarPolicies() {
    }

    /**
     * Parses a HAR content policy name.
     *
     * @param content policy name, one of omit, embed or attach
     * @return matching content policy
     */
    public static HarContentPolicy content(String content) {
        try {
            return HarContentPolicy.valueOf(content.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CitrusRuntimeException("Unsupported Playwright HAR content policy: " + content, e);
        }
    }

    /**
     * Parses a HAR mode name.
     *
     * @param mode mode name, one of full or minimal
     * @return matching HAR mode
     */
    public static HarMode mode(String mode) {
        try {
            return HarMode.valueOf(mode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CitrusRuntimeException("Unsupported Playwright HAR mode: " + mode, e);
        }
    }
}
