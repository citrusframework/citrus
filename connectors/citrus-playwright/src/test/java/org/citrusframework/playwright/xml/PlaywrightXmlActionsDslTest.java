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

import org.citrusframework.Citrus;
import org.citrusframework.TestCase;
import org.citrusframework.playwright.dsl.AbstractDslLoaderTest;
import org.citrusframework.playwright.dsl.PlaywrightActionsAssertions;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * Loads every context, page, network and diagnostics Playwright action from XML.
 */
class PlaywrightXmlActionsDslTest extends AbstractDslLoaderTest {

    @Test
    void shouldLoadDiagnosticsAndContextActions() {
        TestCase result = loadTestCase();

        assertEquals(result.getName(), "PlaywrightActionsDslTest");
        PlaywrightActionsAssertions.assertDiagnosticsAndContextActions(result);
    }

    @Test
    void shouldLoadPageAndNetworkActions() {
        PlaywrightActionsAssertions.assertPageAndNetworkActions(loadTestCase());
    }

    @Test
    void shouldLoadOutputAndTracingActions() {
        PlaywrightActionsAssertions.assertOutputAndTracingActions(loadTestCase());
    }

    private TestCase loadTestCase() {
        bindStubBrowser();
        context.setVariable("username", "foo_user");

        XmlTestLoader testLoader = createXmlTestLoader("playwright-actions.citrus.it");
        // These actions need a live page to run; only the declarative mapping is checked here.
        testLoader.setCitrus(mock(Citrus.class));
        testLoader.load();
        return testLoader.getTestCase();
    }
}
