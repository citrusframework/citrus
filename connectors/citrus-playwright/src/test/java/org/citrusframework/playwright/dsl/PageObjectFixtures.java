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
package org.citrusframework.playwright.dsl;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.page.PlaywrightPage;
import org.citrusframework.playwright.page.PlaywrightPageValidator;

/**
 * Page-object types referenced by class name from the declarative loader tests.
 */
public final class PageObjectFixtures {

    private PageObjectFixtures() {
    }

    public static class FixturePage implements PlaywrightPage {
        public void open() {
            // no-op fixture method
        }
    }

    public static class FixtureValidator implements PlaywrightPageValidator<FixturePage> {
        @Override
        public void validate(FixturePage page, PlaywrightBrowser browser, TestContext context) {
            // no-op fixture validator
        }
    }
}
