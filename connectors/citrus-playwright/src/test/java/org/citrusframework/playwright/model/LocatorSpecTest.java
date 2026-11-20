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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.citrusframework.context.TestContext;
import org.junit.jupiter.api.Test;
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

    @Test
    void shouldResolveRefLocatorOnPageAndFrame() {
        TestContext context = new TestContext();
        context.setVariable("targetRef", "e7");

        Page page = mock(Page.class);
        Locator pageMatch = mock(Locator.class);
        when(page.getByRef("e7")).thenReturn(pageMatch);

        assertSame(pageMatch, LocatorResolver.resolve(page, LocatorSpec.ref("${targetRef}"), context));

        FrameLocator frame = mock(FrameLocator.class);
        Locator frameMatch = mock(Locator.class);
        when(frame.locator("aria-ref=e7")).thenReturn(frameMatch);

        assertSame(frameMatch, LocatorResolver.resolve(frame, LocatorSpec.ref("${targetRef}"), context));
    }

    @Test
    void shouldComposeWithinBeforeVisibleAndPositionalModifiers() {
        Page page = mock(Page.class);
        Locator buttons = mock(Locator.class);
        Locator card = mock(Locator.class);
        Locator scoped = mock(Locator.class);
        Locator visibleScoped = mock(Locator.class);
        Locator firstVisibleScoped = mock(Locator.class);

        when(page.locator("button")).thenReturn(buttons);
        when(page.locator("#card")).thenReturn(card);
        when(buttons.within(card)).thenReturn(scoped);
        when(scoped.visible()).thenReturn(visibleScoped);
        when(visibleScoped.first()).thenReturn(firstVisibleScoped);

        LocatorSpec spec = LocatorSpec.css("button")
                .within(LocatorSpec.css("#card"))
                .visible()
                .first();

        assertSame(firstVisibleScoped, LocatorResolver.resolve(page, spec, new TestContext()));
        assertThrows(NullPointerException.class, () -> LocatorSpec.css("button").within(null));
    }

    @Test
    void shouldComposeWithinInsideFrameLocator() {
        FrameLocator frame = mock(FrameLocator.class);
        Locator items = mock(Locator.class);
        Locator section = mock(Locator.class);
        Locator scoped = mock(Locator.class);

        when(frame.locator(".item")).thenReturn(items);
        when(frame.locator("#section")).thenReturn(section);
        when(items.within(section)).thenReturn(scoped);

        LocatorSpec spec = LocatorSpec.css(".item").within(LocatorSpec.css("#section"));
        assertSame(scoped, LocatorResolver.resolve(frame, spec, new TestContext()));
    }

    @Test
    void shouldResolveAndSubstituteRefAndWithinInPlaywrightTarget() {
        Page page = mock(Page.class);
        Locator refButton = mock(Locator.class);
        Locator container = mock(Locator.class);
        Locator scoped = mock(Locator.class);
        Locator visibleScoped = mock(Locator.class);

        when(page.getByRef("e12")).thenReturn(refButton);
        when(page.locator("#row-42")).thenReturn(container);
        when(refButton.within(container)).thenReturn(scoped);
        when(scoped.visible()).thenReturn(visibleScoped);

        PlaywrightTarget target = PlaywrightTarget.the("Save button")
                .located(LocatorSpec.ref("{0}").within(LocatorSpec.css("#row-{1}")).visible())
                .of("e12", "42");

        assertSame(visibleScoped, target.resolveFor(page));
    }
}
