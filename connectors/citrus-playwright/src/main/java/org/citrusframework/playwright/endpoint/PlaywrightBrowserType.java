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

package org.citrusframework.playwright.endpoint;

import java.util.Locale;

/**
 * Supported Playwright browser engines.
 *
 * <p>Convenience alternative to passing raw engine names such as {@code
 * "chromium"} to {@link PlaywrightEndpointBuilder#browserType(String)} or
 * {@link org.citrusframework.playwright.actions.StartBrowserAction.Builder#browserType(String)}.</p>
 */
public enum PlaywrightBrowserType {

    CHROMIUM,
    FIREFOX,
    WEBKIT;

    /**
     * Returns the engine name understood by the Playwright driver.
     *
     * @return lowercase engine name such as {@code chromium}
     */
    public String getType() {
        return name().toLowerCase(Locale.ROOT);
    }
}
