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

package org.citrusframework.playwright.dsl;

import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.base.context.StaticTestContextFactory;
import org.citrusframework.common.TestSourceHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.playwright.support.StubStartedBrowser;
import org.citrusframework.xml.XmlTestLoader;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;

/**
 * Shared Citrus scaffolding for YAML and XML DSL loader tests.
 */
@ExtendWith(MockitoExtension.class)
@Isolated("XmlTestLoader uses a static non-thread-safe JAXB Unmarshaller cache")
public abstract class AbstractDslLoaderTest extends AbstractUnitTest {

    protected static final String BROWSER_NAME = "playwrightBrowser";

    protected Citrus citrus;

    @Mock
    protected CitrusContext citrusContext;

    @BeforeEach
    @Override
    public void prepareTest() {
        citrus = CitrusInstanceManager.newInstance(() -> citrusContext);

        super.prepareTest();
    }

    @Override
    protected TestContext createTestContext() {
        TestContext context = super.createTestContext();
        lenient().when(citrusContext.getReferenceResolver()).thenReturn(context.getReferenceResolver());
        lenient().when(citrusContext.getMessageValidatorRegistry()).thenReturn(context.getMessageValidatorRegistry());
        lenient().when(citrusContext.getTestContextFactory()).thenReturn(new StaticTestContextFactory(context));
        doAnswer(invocationOnMock -> {
            CitrusAnnotations.parseConfiguration(invocationOnMock.getArgument(0, Object.class), citrusContext);
            return null;
        }).when(citrusContext).parseConfiguration((Object) any());
        lenient().doAnswer(invocationOnMock -> {
            context.getReferenceResolver().bind(invocationOnMock.getArgument(0), invocationOnMock.getArgument(1));
            return null;
        }).when(citrusContext).addComponent(anyString(), any());
        CitrusAnnotations.injectAll(this, citrus, context);
        return context;
    }

    protected StubStartedBrowser bindStubBrowser() {
        StubStartedBrowser browser = new StubStartedBrowser();
        context.getReferenceResolver().bind(BROWSER_NAME, browser);
        return browser;
    }

    protected YamlTestLoader createYamlTestLoader(String testName) {
        YamlTestLoader testLoader = new YamlTestLoader(this.getClass(), testName, this.getClass().getPackageName());
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new NoopTestCaseRunner(context));
        testLoader.setSource(TestSourceHelper.create(
                "classpath:org/citrusframework/playwright/yaml/" + testName + ".yaml"));

        return testLoader;
    }

    protected XmlTestLoader createXmlTestLoader(String testName) {
        XmlTestLoader testLoader = new XmlTestLoader(this.getClass(), testName, "org.citrusframework.playwright.xml");
        CitrusAnnotations.injectAll(testLoader, citrus, context);
        CitrusAnnotations.injectTestRunner(testLoader, new NoopTestCaseRunner(context));
        testLoader.setSource(TestSourceHelper.create(
                "classpath:org/citrusframework/playwright/xml/" + testName + ".xml"));

        return testLoader;
    }

    @SuppressWarnings("unchecked")
    protected static class NoopTestCaseRunner extends DefaultTestCaseRunner {
        public NoopTestCaseRunner(TestContext context) {
            super(context);
        }

        @Override
        public <T extends TestAction> TestActionRunner run(TestActionBuilder<T> builder) {
            builder.build();
            return this;
        }
    }
}
