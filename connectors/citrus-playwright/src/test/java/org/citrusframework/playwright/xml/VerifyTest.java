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

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.playwright.actions.VerifyAction;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * Covers how the XML wrapper properties reach the verification action builder.
 */
class VerifyTest {

    @Test
    void shouldVerifyVisibleByDefault() {
        Verify verify = new Verify();
        verify.setElement(css("#welcome"));

        assertEquals(VerifyAction.Check.VISIBLE, verify.build().getCheck());
    }

    @Test
    void shouldApplyCheckSpecificProperties() {
        Verify verify = new Verify();
        verify.setCheck("attribute");
        verify.setAttribute("data-test");
        verify.setExpected("username");
        verify.setElement(css("#username"));

        assertEquals(VerifyAction.Check.ATTRIBUTE, verify.build().getCheck());
    }

    @Test
    void shouldResolvePageScopedAliasWithoutLocator() {
        Verify verify = new Verify();
        verify.setCheck("page-url");
        verify.setExpected("http://localhost:8080/index.html");

        assertEquals(VerifyAction.Check.URL, verify.build().getCheck());
    }

    @Test
    void shouldFailOnMissingCheckProperty() {
        Verify verify = new Verify();
        verify.setCheck("count");
        verify.setElement(css("#rows"));

        expectThrows(ValidationException.class, verify::build);
    }

    @Test
    void shouldFailOnUnsupportedCheck() {
        Verify verify = new Verify();
        verify.setCheck("not-a-check");
        verify.setElement(css("#rows"));

        expectThrows(IllegalArgumentException.class, verify::build);
    }

    private static Element css(String selector) {
        Element element = new Element();
        element.setCss(selector);
        return element;
    }
}
