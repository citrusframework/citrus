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
package org.citrusframework.cucumber.backend;

import java.util.Optional;

import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.discovery.GlueDiscoveryRequest;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.citrusframework.report.TestSuiteListener;
import org.citrusframework.report.TestSuiteListeners;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * Uses its own Citrus instance on a mocked context instead of the shared Spring test context, and puts the previous
 * instance back without closing anything, so other test classes keep their instance and application context.
 */
public class CitrusSuiteHooksTest {

    private TestSuiteListener suiteListener;

    private CitrusContext citrusContext;

    private Citrus originalInstance;

    @BeforeMethod
    public void createCitrus() {
        originalInstance = CitrusInstanceManager.get().orElse(null);

        suiteListener = mock(TestSuiteListener.class);
        TestSuiteListeners suiteListeners = new TestSuiteListeners();
        suiteListeners.addTestSuiteListener(suiteListener);

        citrusContext = mock(CitrusContext.class);
        when(citrusContext.getTestSuiteListeners()).thenReturn(suiteListeners);

        CitrusInstanceManager.newInstance(() -> citrusContext);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreInstance() {
        setInstance(originalInstance);
    }

    @Test
    public void shouldStartAndFinishSuiteOnExistingInstance() {
        CitrusLifecycleHooks.beforeSuite();
        verify(suiteListener).onStart();

        CitrusLifecycleHooks.afterSuite();
        verify(suiteListener).onFinish();
    }

    @Test
    public void shouldStartSuiteOnceOnNextCreatedInstance() {
        setInstance(null);

        CitrusLifecycleHooks.beforeSuite();
        assertFalse(CitrusInstanceManager.hasInstance());
        verify(suiteListener, never()).onStart();

        CitrusInstanceManager.newInstance(() -> citrusContext);
        verify(suiteListener).onStart();

        CitrusInstanceManager.newInstance(() -> citrusContext);
        verify(suiteListener, times(1)).onStart();
    }

    @Test
    public void shouldStartPendingSuiteOnceWhenRunTwice() {
        setInstance(null);

        CitrusLifecycleHooks.beforeSuite();
        CitrusLifecycleHooks.beforeSuite();
        CitrusInstanceManager.newInstance(() -> citrusContext);

        verify(suiteListener, times(1)).onStart();
    }

    @Test
    public void shouldNotCreateInstanceOnAfterSuite() {
        setInstance(null);

        CitrusLifecycleHooks.beforeSuite();
        CitrusLifecycleHooks.afterSuite();
        assertFalse(CitrusInstanceManager.hasInstance());

        CitrusInstanceManager.newInstance(() -> citrusContext);
        verify(suiteListener, never()).onStart();
        verify(suiteListener, never()).onFinish();
    }

    @Test
    public void shouldRegisterSuiteHooksOnGlue() {
        Container container = mock(Container.class);
        when(container.addClass(CitrusLifecycleHooks.class)).thenReturn(true);
        Glue glue = mock(Glue.class);

        new CitrusBackend(mock(Lookup.class), container).loadGlue(glue, mock(GlueDiscoveryRequest.class));

        ArgumentCaptor<StaticHookDefinition> beforeAll = ArgumentCaptor.forClass(StaticHookDefinition.class);
        ArgumentCaptor<StaticHookDefinition> afterAll = ArgumentCaptor.forClass(StaticHookDefinition.class);
        verify(glue).addBeforeAllHook(beforeAll.capture());
        verify(glue).addAfterAllHook(afterAll.capture());
        verify(glue).addBeforeHook(any(HookDefinition.class));
        verify(glue).addAfterHook(any(HookDefinition.class));

        assertEquals(beforeAll.getValue().getName(), Optional.of("CitrusLifecycleHooks.beforeSuite"));
        assertEquals(beforeAll.getValue().getOrder(), CitrusLifecycleHooks.SUITE_HOOK_ORDER);
        assertEquals(afterAll.getValue().getOrder(), CitrusLifecycleHooks.SUITE_HOOK_ORDER);
        assertEquals(beforeAll.getValue().getHookType(), Optional.of(StaticHookDefinition.HookType.BEFORE_ALL));
        assertEquals(afterAll.getValue().getName(), Optional.of("CitrusLifecycleHooks.afterSuite"));
        assertEquals(afterAll.getValue().getHookType(), Optional.of(StaticHookDefinition.HookType.AFTER_ALL));
    }

    private static void setInstance(Citrus instance) {
        ReflectionTestUtils.setField(CitrusInstanceManager.class, "citrus", instance);
    }
}
