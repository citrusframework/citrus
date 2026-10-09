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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.options.AriaRole;

import java.util.Locale;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.LocatorSpec;

public final class LocatorResolver {

    private LocatorResolver() {
    }

    public static Locator resolve(Page page, LocatorSpec spec, TestContext context) {
        Locator locator = switch (spec.getType()) {
            case CSS -> page.locator(resolve(spec.getSelector(), context));
            case XPATH -> page.locator("xpath=" + resolve(spec.getSelector(), context));
            case TEXT -> page.getByText(resolve(spec.getSelector(), context));
            case ROLE -> page.getByRole(resolveRole(spec.getSelector(), context),
                    new Page.GetByRoleOptions().setName(resolve(spec.getName(), context)));
            case TEST_ID -> page.getByTestId(resolve(spec.getSelector(), context));
            case RAW -> spec.getRawLocator().apply(page);
        };

        return applyModifiers(locator, spec);
    }

    public static Locator resolve(FrameLocator frame, LocatorSpec spec, TestContext context) {
        Locator locator = switch (spec.getType()) {
            case CSS -> frame.locator(resolve(spec.getSelector(), context));
            case XPATH -> frame.locator("xpath=" + resolve(spec.getSelector(), context));
            case TEXT -> frame.getByText(resolve(spec.getSelector(), context));
            case ROLE -> frame.getByRole(resolveRole(spec.getSelector(), context),
                    new FrameLocator.GetByRoleOptions().setName(resolve(spec.getName(), context)));
            case TEST_ID -> frame.getByTestId(resolve(spec.getSelector(), context));
            case RAW -> throw new CitrusRuntimeException("Raw page locators are not supported inside Playwright frame actions");
        };

        return applyModifiers(locator, spec);
    }

    /**
     * Applies the modifiers declared on the locator spec. Shared by the page and frame resolution
     * paths so both stay in sync. The visibility filter narrows the match set first, so any
     * positional modifier then selects within the visible elements only.
     *
     * @param locator resolved base locator
     * @param spec locator spec holding the modifiers
     * @return narrowed locator
     */
    private static Locator applyModifiers(Locator locator, LocatorSpec spec) {
        Locator resolved = spec.isVisible() ? locator.visible() : locator;

        if (spec.getNth() != null) {
            return resolved.nth(spec.getNth());
        }
        if (spec.isFirst()) {
            return resolved.first();
        }
        if (spec.isLast()) {
            return resolved.last();
        }
        return resolved;
    }

    private static AriaRole resolveRole(String role, TestContext context) {
        String value = resolve(role, context);
        try {
            return AriaRole.valueOf(value.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new CitrusRuntimeException("Unsupported Playwright ARIA role: " + value, e);
        }
    }

    public static String resolve(String value, TestContext context) {
        return value == null ? null : context.replaceDynamicContentInString(value);
    }
}
