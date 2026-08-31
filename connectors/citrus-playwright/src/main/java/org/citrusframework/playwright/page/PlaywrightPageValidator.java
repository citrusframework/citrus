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

package org.citrusframework.playwright.page;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;

/**
 * Validation extension point for page objects created by the Citrus Playwright DSL.
 *
 * @param <T> page-object type
 */
public interface PlaywrightPageValidator<T extends PlaywrightPage> {

    /**
     * Validates a page object after it has been initialized and optionally invoked.
     *
     * @param page page-object instance
     * @param browser active Playwright endpoint
     * @param context active Citrus test context
     */
    void validate(T page, PlaywrightBrowser browser, TestContext context);
}
