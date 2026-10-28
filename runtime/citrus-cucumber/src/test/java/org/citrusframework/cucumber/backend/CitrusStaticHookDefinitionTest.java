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

import java.lang.reflect.Method;
import java.util.Optional;

import io.cucumber.core.backend.StaticHookDefinition.HookType;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CitrusStaticHookDefinitionTest {

    @Test
    public void shouldExposeBeforeSuiteHookMetadata() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(
                CitrusLifecycleHooks.class.getMethod("beforeSuite"), 10000, HookType.BEFORE_ALL);

        assertEquals(hook.getName(), Optional.of("CitrusLifecycleHooks.beforeSuite"));
        assertEquals(hook.getHookType(), Optional.of(HookType.BEFORE_ALL));
        assertEquals(hook.getOrder(), 10000);
        assertEquals(hook.getLocation(), "org.citrusframework.cucumber.CitrusLifecycleHooks#beforeSuite");
    }

    @Test
    public void shouldExposeAfterSuiteHookMetadata() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(
                CitrusLifecycleHooks.class.getMethod("afterSuite"), 10000, HookType.AFTER_ALL);

        assertEquals(hook.getName(), Optional.of("CitrusLifecycleHooks.afterSuite"));
        assertEquals(hook.getHookType(), Optional.of(HookType.AFTER_ALL));
        assertEquals(hook.getOrder(), 10000);
    }

    @Test
    public void shouldInvokeStaticMethod() throws NoSuchMethodException {
        HookMethods.invocations = 0;
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(method("count"), 10000, HookType.BEFORE_ALL);

        hook.execute();

        assertEquals(HookMethods.invocations, 1);
    }

    @Test
    public void shouldMatchStackTraceOfHookMethod() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(method("count"), 10000, HookType.BEFORE_ALL);

        assertTrue(hook.isDefinedAt(new StackTraceElement(HookMethods.class.getName(), "count", null, 0)));
        assertFalse(hook.isDefinedAt(new StackTraceElement(HookMethods.class.getName(), "other", null, 0)));
    }

    private static Method method(String name) throws NoSuchMethodException {
        return HookMethods.class.getMethod(name);
    }

    public static class HookMethods {

        static int invocations;

        public static void count() {
            invocations++;
        }
    }
}
