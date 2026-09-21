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

package org.citrusframework.playwright.yaml;

import org.citrusframework.playwright.model.LocatorSpec;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Covers how the shared YAML locator element translates into a {@link LocatorSpec}.
 */
class ElementTest {

    @Test
    void shouldNotFilterByVisibilityByDefault() {
        Element element = new Element();
        element.setCss("#save");

        assertFalse(element.toLocatorSpec().isVisible());
    }

    @Test
    void shouldTranslateVisibleAttribute() {
        Element element = new Element();
        element.setCss("button");
        element.setVisible(true);

        assertTrue(element.toLocatorSpec().isVisible());
    }

    @Test
    void shouldCombineVisibleWithPositionalModifier() {
        Element element = new Element();
        element.setCss(".row");
        element.setVisible(true);
        element.setNth(2);

        LocatorSpec spec = element.toLocatorSpec();

        assertTrue(spec.isVisible());
        assertEquals(2, spec.getNth());
    }
}
