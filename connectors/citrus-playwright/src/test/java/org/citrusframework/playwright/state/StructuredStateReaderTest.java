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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.playwright.model.BoundingBoxResult;
import org.citrusframework.playwright.model.SecretPatternRedactor;
import org.testng.annotations.Test;

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
