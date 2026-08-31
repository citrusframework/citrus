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

package org.citrusframework.playwright.actions;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.citrusframework.playwright.actions.VerifyAction.Check;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

/**
 * Covers the DSL check name resolution shared by the XML and YAML wrappers.
 */
class VerifyCheckNameTest {

    private static final Set<Check> PAGE_SCOPED = EnumSet.of(Check.URL, Check.TITLE, Check.PAGE_COUNT,
            Check.FRAME_CONTENT_CONTAINS, Check.STORAGE_LOCAL, Check.STORAGE_SESSION, Check.COOKIE,
            Check.CONSOLE_CONTAINS, Check.NETWORK_URL_CONTAINS, Check.DOWNLOAD_FILENAME, Check.DOWNLOAD_PATH);

    @Test
    void shouldResolveEveryCheckFromItsHyphenatedName() {
        for (Check check : Check.values()) {
            String dslName = check.name().toLowerCase(Locale.ENGLISH).replace('_', '-');
            assertEquals(check, Check.fromName(dslName), dslName);
            assertEquals(check, Check.fromName("  " + check.name() + "  "), check.name());
        }
    }

    @Test
    void shouldResolvePageScopedAliases() {
        assertEquals(Check.URL, Check.fromName("page-url"));
        assertEquals(Check.TITLE, Check.fromName("page_title"));
    }

    @Test
    void shouldRejectUnknownCheckName() {
        IllegalArgumentException exception = expectThrows(IllegalArgumentException.class,
                () -> Check.fromName("does-not-exist"));
        assertEquals("Unsupported Playwright verify check: does-not-exist", exception.getMessage());
    }

    @Test
    void shouldReportLocatorRequirementPerCheck() {
        for (Check check : Check.values()) {
            if (PAGE_SCOPED.contains(check)) {
                assertFalse(check.requiresLocator(), check.name());
            } else {
                assertTrue(check.requiresLocator(), check.name());
            }
        }
    }
}
