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

package org.citrusframework.playwright.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaSnapshotMode;
import com.microsoft.playwright.options.BoundingBox;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.citrusframework.playwright.util.AriaSnapshotModes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StructuredStateReaderTest {

    @Test
    void shouldReadLocatorStateAsStableValues() {
        Locator locator = locator(Map.of(
                "boundingBox", boundingBox(10, 20, 30, 40),
                "allTextContents", List.of("One", "Two"),
                "allInnerTexts", List.of("Inner One", "Inner Two"),
                "innerHTML", "<span class=\"secret-token\">Value</span>",
                "getAttribute", "btn primary secret-token",
                "ariaSnapshot", "- button \"Submit\""));

        LocatorStateReader reader = new LocatorStateReader(new SecretPatternRedactor(List.of("secret-token")));

        assertEquals(new BoundingBoxResult(10, 20, 30, 40), reader.boundingBox(locator));
        assertEquals(List.of("One", "Two"), reader.allTextContents(locator));
        assertEquals(List.of("Inner One", "Inner Two"), reader.allInnerTexts(locator));
        assertEquals("<span class=\"" + SecretPatternRedactor.MASK + "\">Value</span>", reader.innerHtml(locator));
        assertEquals(List.of("btn", "primary", SecretPatternRedactor.MASK), reader.cssClasses(locator));
        assertEquals("- button \"Submit\"", reader.ariaSnapshot(locator));
    }

    @Test
    void shouldReturnNullBoundingBoxWhenElementIsNotVisible() {
        LocatorStateReader reader = new LocatorStateReader(new SecretPatternRedactor(List.of()));

        Map<String, Object> values = new HashMap<>();
        values.put("boundingBox", null);

        assertNull(reader.boundingBox(locator(values)));
    }

    @Test
    void shouldReadAriaSnapshotOptionsAndNormalizedLocatorWithRedaction() {
        SecretPatternRedactor redactor = new SecretPatternRedactor(List.of("secret-ref"));
        LocatorStateReader locatorReader = new LocatorStateReader(redactor);
        PageStateReader pageReader = new PageStateReader(redactor);

        Locator mockLocator = mock(Locator.class);
        Locator normalized = (Locator) Proxy.newProxyInstance(
                Locator.class.getClassLoader(),
                new Class<?>[]{Locator.class},
                (proxy, method, args) -> "toString".equals(method.getName())
                        ? "getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"secret-ref\"))"
                        : defaultValue(method.getReturnType()));

        when(mockLocator.ariaSnapshot(any(Locator.AriaSnapshotOptions.class)))
                .thenReturn("- button \"Submit\" [ref=e2] secret-ref");
        when(mockLocator.normalize()).thenReturn(normalized);

        String locatorSnap = locatorReader.ariaSnapshot(mockLocator, AriaSnapshotMode.AI, true, 2, 1500D);
        assertEquals("- button \"Submit\" [ref=e2] " + SecretPatternRedactor.MASK, locatorSnap);

        ArgumentCaptor<Locator.AriaSnapshotOptions> locatorOpts = ArgumentCaptor.forClass(Locator.AriaSnapshotOptions.class);
        verify(mockLocator).ariaSnapshot(locatorOpts.capture());
        assertEquals(AriaSnapshotMode.AI, locatorOpts.getValue().mode);
        assertEquals(Boolean.TRUE, locatorOpts.getValue().boxes);
        assertEquals(Integer.valueOf(2), locatorOpts.getValue().depth);
        assertEquals(Double.valueOf(1500D), locatorOpts.getValue().timeout);

        assertEquals(
                "getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"" + SecretPatternRedactor.MASK + "\"))",
                locatorReader.normalizedLocator(mockLocator));

        Page mockPage = mock(Page.class);
        when(mockPage.ariaSnapshot(any(Page.AriaSnapshotOptions.class)))
                .thenReturn("- main [ref=e1]: secret-ref");

        String pageSnap = pageReader.ariaSnapshot(mockPage, AriaSnapshotMode.AI, false, 3, 2500D);
        assertEquals("- main [ref=e1]: " + SecretPatternRedactor.MASK, pageSnap);

        ArgumentCaptor<Page.AriaSnapshotOptions> pageOpts = ArgumentCaptor.forClass(Page.AriaSnapshotOptions.class);
        verify(mockPage).ariaSnapshot(pageOpts.capture());
        assertEquals(AriaSnapshotMode.AI, pageOpts.getValue().mode);
        assertEquals(Boolean.FALSE, pageOpts.getValue().boxes);
        assertEquals(Integer.valueOf(3), pageOpts.getValue().depth);
        assertEquals(Double.valueOf(2500D), pageOpts.getValue().timeout);
    }

    @Test
    void shouldReadPageContentWithShadowSerializationAndRedaction() {
        SecretPatternRedactor redactor = new SecretPatternRedactor(List.of("secret-ref"));
        PageStateReader pageReader = new PageStateReader(redactor);
        Page mockPage = mock(Page.class);

        when(mockPage.content()).thenReturn("<html><body>secret-ref</body></html>");
        when(mockPage.content(any(Page.ContentOptions.class)))
                .thenReturn("<html><body><template shadowrootmode=\"open\">secret-ref</template></body></html>");

        assertEquals("<html><body>" + SecretPatternRedactor.MASK + "</body></html>",
                pageReader.pageContent(mockPage, null));
        assertEquals(
                "<html><body><template shadowrootmode=\"open\">" + SecretPatternRedactor.MASK + "</template></body></html>",
                pageReader.pageContent(mockPage, true));

        ArgumentCaptor<Page.ContentOptions> contentOpts = ArgumentCaptor.forClass(Page.ContentOptions.class);
        verify(mockPage).content(contentOpts.capture());
        assertEquals(Boolean.TRUE, contentOpts.getValue().includeShadow);
    }

    @Test
    void shouldParseAriaSnapshotModes() {
        assertNull(AriaSnapshotModes.parse(null));
        assertNull(AriaSnapshotModes.parse("  "));
        assertEquals(AriaSnapshotMode.AI, AriaSnapshotModes.parse("ai"));
        assertEquals(AriaSnapshotMode.AI, AriaSnapshotModes.parse("AI"));
        assertEquals(AriaSnapshotMode.DEFAULT, AriaSnapshotModes.parse("default"));
        assertThrows(CitrusRuntimeException.class, () -> AriaSnapshotModes.parse("custom"));
    }

    @SuppressWarnings("unchecked")
    private Locator locator(Map<String, Object> values) {
        return (Locator) Proxy.newProxyInstance(Locator.class.getClassLoader(), new Class<?>[]{Locator.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "boundingBox" -> values.get("boundingBox");
                    case "allTextContents" -> values.get("allTextContents");
                    case "allInnerTexts" -> values.get("allInnerTexts");
                    case "innerHTML" -> values.get("innerHTML");
                    case "getAttribute" -> values.get("getAttribute");
                    case "ariaSnapshot" -> values.get("ariaSnapshot");
                    case "evaluate" -> evaluate((String) args[0], values);
                    default -> defaultValue(method.getReturnType());
                });
    }

    private BoundingBox boundingBox(double x, double y, double width, double height) {
        BoundingBox box = new BoundingBox();
        box.x = x;
        box.y = y;
        box.width = width;
        box.height = height;
        return box;
    }

    private Object evaluate(String expression, Map<String, Object> values) {
        if (expression.contains("classList")) {
            return List.of("btn", "primary", "secret-token");
        }
        if (expression.contains("getComputedStyle")) {
            return "block";
        }
        if (expression.contains("options")) {
            return List.of("Pending", "Ready");
        }
        if (expression.contains("selectedOptions")) {
            return "Ready";
        }
        return values.get("evaluate");
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        return 0;
    }
}
