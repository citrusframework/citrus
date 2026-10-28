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
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.citrusframework.cucumber.UnitTestSupport;
import org.citrusframework.report.TestSuiteListener;
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

public class CitrusSuiteHooksTest extends UnitTestSupport {

    private TestSuiteListener suiteListener;

    private Optional<Citrus> originalInstance;

    @BeforeMethod
    public void registerSuiteListener() {
        originalInstance = CitrusInstanceManager.get();
        suiteListener = mock(TestSuiteListener.class);
        citrus.getCitrusContext().getTestSuiteListeners().addTestSuiteListener(suiteListener);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreInstance() {
        // Listener list is unmodifiable, the inert mock stays registered
        setInstance(originalInstance.orElse(null));
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

        CitrusInstanceManager.newInstance(citrus::getCitrusContext);
        verify(suiteListener).onStart();

        CitrusInstanceManager.newInstance(citrus::getCitrusContext);
        verify(suiteListener, times(1)).onStart();
    }

    @Test
    public void shouldStartPendingSuiteOnceWhenRunTwice() {
        setInstance(null);

        CitrusLifecycleHooks.beforeSuite();
        CitrusLifecycleHooks.beforeSuite();
        CitrusInstanceManager.newInstance(citrus::getCitrusContext);

        verify(suiteListener, times(1)).onStart();
    }

    @Test
    public void shouldNotCreateInstanceOnAfterSuite() {
        setInstance(null);

        CitrusLifecycleHooks.beforeSuite();
        CitrusLifecycleHooks.afterSuite();
        assertFalse(CitrusInstanceManager.hasInstance());

        CitrusInstanceManager.newInstance(citrus::getCitrusContext);
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
