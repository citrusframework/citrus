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

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.container.Sequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

class ScopedBlockTest {

    private final StubStartedBrowser browser = new StubStartedBrowser();

    @BeforeMethod
    void setUp() {
        browser.start();
    }

    @AfterMethod
    void tearDown() {
        PlaywrightBrowserScope.clear();
        browser.stop();
    }

    @Test
    void shouldCollectChainedActionsAsSequentialBlock() {
        Sequence block = PlaywrightActionBuilder.with(browser, pw -> {
            pw.open().url("http://localhost:8080/index.html");
            pw.javascript().script("() => document.title");
            pw.console().capture();
        });

        assertEquals(3, block.getActionCount());
        List<TestAction> actions = block.getActions();
        assertEquals(actions.get(0).getClass(), OpenAction.class);
        assertEquals(actions.get(1).getClass(), JavaScriptAction.class);
        assertEquals(actions.get(2).getClass(), ConsoleAction.class);
    }

    @Test
    void shouldExecuteBlockWithoutTouchingAmbientScope() {
        Sequence block = PlaywrightActionBuilder.with(browser, pw -> {
            pw.open().url("http://localhost:8080/index.html");
            pw.javascript().script("() => 1");
        });

        TestContext context = new TestContext();
        block.execute(context);

        assertTrue(PlaywrightBrowserScope.current(context).isEmpty());
    }
}
