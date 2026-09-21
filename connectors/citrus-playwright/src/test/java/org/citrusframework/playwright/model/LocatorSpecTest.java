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

package org.citrusframework.playwright.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.citrusframework.context.TestContext;
import org.testng.annotations.Test;
import org.citrusframework.playwright.util.LocatorResolver;

class LocatorSpecTest {

    @Test
    void shouldModelCssLocatorWithIndex() {
        LocatorSpec locator = LocatorSpec.css(".row").nth(2);

        assertEquals(LocatorSpec.Type.CSS, locator.getType());
        assertEquals(".row", locator.getSelector());
        assertEquals(2, locator.getNth());
    }

    @Test
    void shouldModelRoleLocator() {
        LocatorSpec locator = LocatorSpec.role("button").name("Save").first();

        assertEquals(LocatorSpec.Type.ROLE, locator.getType());
        assertEquals("button", locator.getSelector());
        assertEquals("Save", locator.getName());
        assertTrue(locator.isFirst());
    }

    @Test
    void shouldModelVisibleOnlyLocator() {
        LocatorSpec locator = LocatorSpec.css("button").visible();

        assertTrue(locator.isVisible());
        assertFalse(locator.isFirst());
        assertFalse(locator.isLast());
        assertEquals(null, locator.getNth());
    }

    @Test
    void shouldKeepVisibleFilterOrthogonalToPositionalModifiers() {
        LocatorSpec locator = LocatorSpec.css(".row").visible().nth(1);

        assertTrue(locator.isVisible());
        assertEquals(1, locator.getNth());

        assertTrue(LocatorSpec.css(".row").nth(1).visible().isVisible());
        assertTrue(LocatorSpec.css(".row").first().visible().isFirst());
    }

    @Test
    void shouldApplyVisibleFilterWhenResolvingLocator() {
        Page page = mock(Page.class);
        Locator base = mock(Locator.class);
        Locator visibleOnly = mock(Locator.class);
        when(page.locator("button")).thenReturn(base);
        when(base.visible()).thenReturn(visibleOnly);

        Locator resolved = LocatorResolver.resolve(page, LocatorSpec.css("button").visible(), new TestContext());

        assertEquals(visibleOnly, resolved);
    }

    @Test
    void shouldApplyVisibleFilterBeforePositionalModifier() {
        Page page = mock(Page.class);
        Locator base = mock(Locator.class);
        Locator visibleOnly = mock(Locator.class);
        Locator secondVisible = mock(Locator.class);
        when(page.locator(".row")).thenReturn(base);
        when(base.visible()).thenReturn(visibleOnly);
        when(visibleOnly.nth(1)).thenReturn(secondVisible);

        Locator resolved = LocatorResolver.resolve(page, LocatorSpec.css(".row").visible().nth(1), new TestContext());

        assertEquals(secondVisible, resolved);
    }

    @Test
    void shouldResolveDynamicValuesFromContext() {
        TestContext context = new TestContext();
        context.setVariable("selector", "submit");

        assertEquals("#submit", LocatorResolver.resolve("#${selector}", context));
    }
}
