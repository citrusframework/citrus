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

import org.citrusframework.TestActor;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.Producer;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.endpoint.PlaywrightHeaders;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

class BrowserResolutionTest {

    private final StubStartedBrowser browser = new StubStartedBrowser();
    private final TestContext context = new TestContext();

    @BeforeMethod
    void setUp() {
        context.setReferenceResolver(new SimpleReferenceResolver());
    }

    @AfterMethod
    void tearDown() {
        PlaywrightBrowserScope.clear();
    }

    @Test
    void shouldPreferExplicitBindingOverAmbientScope() {
        StubStartedBrowser ambient = new StubStartedBrowser();
        ambient.start();
        PlaywrightBrowserScope.bind(ambient, context);

        assertSame(browser, new ProbeAction.ProbeBuilder().browser(browser).build().resolveFor(context));
    }

    @Test
    void shouldUseAmbientScopeWhenNoExplicitBinding() {
        browser.start();
        PlaywrightBrowserScope.bind(browser, context);

        assertSame(browser, probe().resolveFor(context));
    }

    @Test
    void shouldFallBackToVariableWhenNoExplicitBindingOrScope() {
        context.getReferenceResolver().bind("myBrowser", browser);
        context.setVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER, "myBrowser");

        assertSame(browser, probe().resolveFor(context));
    }

    @Test
    void shouldFallBackToUniqueBeanWhenNoExplicitBindingScopeOrVariable() {
        context.getReferenceResolver().bind("myBrowser", browser);

        assertSame(browser, probe().resolveFor(context));
    }

    @Test
    void shouldFailWithStrategyReportWhenNothingResolvable() {
        ProbeAction probe = probe();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> probe.resolveFor(context));

        assertTrue(exception.getMessage().contains("explicit .browser(<endpoint>) binding: not set"));
        assertTrue(exception.getMessage().contains("thread scope"));
        assertTrue(exception.getMessage().contains(PlaywrightHeaders.PLAYWRIGHT_BROWSER));
        assertTrue(exception.getMessage().contains("unique %s bean".formatted(PlaywrightBrowser.class.getSimpleName())));
    }

    @Test
    void shouldResolveExplicitlyNamedEndpoint() {
        context.getReferenceResolver().bind("adminBrowser", browser);

        assertSame(browser, new ProbeAction.ProbeBuilder().browser("adminBrowser").build().resolveFor(context));
    }

    @Test
    void shouldPreferExplicitNameOverAmbientScope() {
        StubStartedBrowser ambient = new StubStartedBrowser();
        ambient.start();
        PlaywrightBrowserScope.bind(ambient, context);
        context.getReferenceResolver().bind("adminBrowser", browser);

        assertSame(browser, new ProbeAction.ProbeBuilder().browser("adminBrowser").build().resolveFor(context));
    }

    @Test
    void shouldFailFastWhenExplicitNameIsUnresolvable() {
        ProbeAction probe = new ProbeAction.ProbeBuilder().browser("missing-browser").build();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> probe.resolveFor(context));

        assertTrue(exception.getMessage().contains("named 'missing-browser'"));
        assertFalse(exception.getMessage().contains("Attempted strategies"));
    }

    @Test
    void shouldReportVariableThatIsSetButNotResolvable() {
        context.setVariable(PlaywrightHeaders.PLAYWRIGHT_BROWSER, "ghostBrowser");

        ProbeAction probe = probe();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> probe.resolveFor(context));

        // The report has to distinguish "variable not set" from "set but pointing at
        // nothing", otherwise a typo'd endpoint name looks identical to no binding.
        assertTrue(exception.getMessage().contains("set to 'ghostBrowser' but not resolvable"));
    }

    @Test
    void shouldBindBrowserGivenAsAnEndpointObject() {
        assertSame(browser, new ProbeAction.ProbeBuilder().browser((Endpoint) browser).build().resolveFor(context));
    }

    @Test
    void shouldRejectEndpointObjectThatIsNotAPlaywrightBrowser() {
        NotAPlaywrightBrowser notABrowser = new NotAPlaywrightBrowser();
        ProbeAction.ProbeBuilder builder = new ProbeAction.ProbeBuilder();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> builder.browser(notABrowser));

        assertTrue(exception.getMessage().contains("Invalid browser object"));
        assertTrue(exception.getMessage().contains(NotAPlaywrightBrowser.class.getName()));
    }

    @Test
    void shouldNotResolveABrowserLeftBehindByAPreviousTest() {
        // Test 1: start a browser and never reach stop(), as a failing test would.
        TestContext firstTest = new TestContext();
        firstTest.setReferenceResolver(new SimpleReferenceResolver());
        new StartBrowserAction.Builder().browser(browser).build().execute(firstTest);
        assertTrue(browser.isStarted());

        // Test 2 reuses the same thread with its own context and no binding of its
        // own. Resolution must fail loudly rather than silently inheriting the
        // previous test's browser, which is the contamination this guards against.
        TestContext secondTest = new TestContext();
        secondTest.setReferenceResolver(new SimpleReferenceResolver());

        ProbeAction probe = probe();

        CitrusRuntimeException exception = expectThrows(CitrusRuntimeException.class,
                () -> probe.resolveFor(secondTest));

        assertTrue(exception.getMessage().contains("thread scope"));
        assertFalse(PlaywrightBrowserScope.isBoundTo(browser),
                "the stale binding is dropped, not carried into the next test");
    }

    private ProbeAction probe() {
        return new ProbeAction.ProbeBuilder().build();
    }

    static class ProbeAction extends AbstractPlaywrightAction {

        private PlaywrightBrowser resolved;

        ProbeAction(ProbeBuilder builder) {
            super("probe", builder);
        }

        PlaywrightBrowser resolveFor(TestContext testContext) {
            doExecute(testContext);
            return resolved;
        }

        @Override
        protected void execute(PlaywrightBrowser resolvedBrowser, TestContext testContext) {
            this.resolved = resolvedBrowser;
        }

        static class ProbeBuilder extends AbstractPlaywrightAction.Builder<ProbeAction, ProbeBuilder> {
            @Override
            public ProbeAction build() {
                return new ProbeAction(this);
            }
        }
    }

    /** Minimal non-Playwright endpoint, so the type guard has something to reject. */
    static class NotAPlaywrightBrowser implements Endpoint {

        private String name = "not-a-browser";

        @Override
        public Producer createProducer() {
            return null;
        }

        @Override
        public Consumer createConsumer() {
            return null;
        }

        @Override
        public EndpointConfiguration getEndpointConfiguration() {
            return null;
        }

        @Override
        public TestActor getActor() {
            return null;
        }

        @Override
        public void setActor(TestActor actor) {
            // no actor support needed for the type guard
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }
    }
}
