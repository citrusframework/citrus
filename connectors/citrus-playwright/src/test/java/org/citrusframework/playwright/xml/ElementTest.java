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

package org.citrusframework.playwright.xml;

import java.io.StringReader;

import jakarta.xml.bind.JAXBContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.actions.ExtractAction;
import org.citrusframework.playwright.model.LocatorSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers how the shared XML locator element translates into a {@link LocatorSpec}.
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

    @Test
    void shouldTranslateRefAndNestedWithin() throws Exception {
        Element withinCard = new Element();
        withinCard.setCss("#card");
        Element withinPanel = new Element();
        withinPanel.setCss("#panel");
        withinCard.setWithin(withinPanel);

        Element element = new Element();
        element.setRef("e4");
        element.setWithin(withinCard);

        LocatorSpec spec = element.toLocatorSpec();
        assertEquals(LocatorSpec.Type.REF, spec.getType());
        assertEquals("e4", spec.getSelector());
        assertNotNull(spec.getWithin());
        assertEquals("#card", spec.getWithin().getSelector());
        assertNotNull(spec.getWithin().getWithin());
        assertEquals("#panel", spec.getWithin().getWithin().getSelector());

        JAXBContext jaxb = JAXBContext.newInstance(Playwright.class);
        Playwright parsed = (Playwright) jaxb.createUnmarshaller().unmarshal(new StringReader("""
                <playwright xmlns="http://citrusframework.org/schema/xml/testcase">
                    <extract value="normalized-locator" variable="norm">
                        <element role="button" name="Save" visible="true">
                            <within ref="e9"/>
                        </element>
                    </extract>
                </playwright>
                """));
        ExtractAction extract = (ExtractAction) parsed.build();
        assertEquals(LocatorSpec.Type.ROLE, extract.getLocator().getType());
        assertEquals("Save", extract.getLocator().getName());
        assertTrue(extract.getLocator().isVisible());
        assertNotNull(extract.getLocator().getWithin());
        assertEquals(LocatorSpec.Type.REF, extract.getLocator().getWithin().getType());
        assertEquals("e9", extract.getLocator().getWithin().getSelector());

        CitrusRuntimeException missing = assertThrows(CitrusRuntimeException.class, () -> new Element().toLocatorSpec());
        assertTrue(missing.getMessage().contains("ref"));
    }
}
