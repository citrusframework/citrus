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

import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.StaticHookDefinition.HookType;
import org.citrusframework.cucumber.CitrusLifecycleHooks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CitrusStaticHookDefinitionTest {

    @Test
    public void shouldExposeBeforeSuiteHookMetadata() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(
                CitrusLifecycleHooks.class.getMethod("beforeSuite"), 10000, HookType.BEFORE_ALL);

        Assertions.assertEquals(Optional.of("CitrusLifecycleHooks.beforeSuite"), hook.getName());
        Assertions.assertEquals(Optional.of(HookType.BEFORE_ALL), hook.getHookType());
        Assertions.assertEquals(10000, hook.getOrder());
        Assertions.assertEquals("org.citrusframework.cucumber.CitrusLifecycleHooks#beforeSuite", hook.getLocation());
    }

    @Test
    public void shouldExposeAfterSuiteHookMetadata() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(
                CitrusLifecycleHooks.class.getMethod("afterSuite"), 10000, HookType.AFTER_ALL);

        Assertions.assertEquals(Optional.of("CitrusLifecycleHooks.afterSuite"), hook.getName());
        Assertions.assertEquals(Optional.of(HookType.AFTER_ALL), hook.getHookType());
        Assertions.assertEquals(10000, hook.getOrder());
    }

    @Test
    public void shouldInvokeStaticMethod() throws NoSuchMethodException {
        HookMethods.invocations = 0;
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(method("count"), 10000, HookType.BEFORE_ALL);

        hook.execute();

        Assertions.assertEquals(1, HookMethods.invocations);
    }

    @Test
    public void shouldReportHookFailureWithHookLocation() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(method("fail"), 10000, HookType.AFTER_ALL);

        CucumberInvocationTargetException exception = Assertions.assertThrows(CucumberInvocationTargetException.class, hook::execute);

        Assertions.assertSame(hook, exception.getLocated());
        Assertions.assertEquals("Suite action failed", exception.getInvocationTargetExceptionCause().getMessage());
    }

    @Test
    public void shouldMatchStackTraceOfHookMethod() throws NoSuchMethodException {
        CitrusStaticHookDefinition hook = new CitrusStaticHookDefinition(method("count"), 10000, HookType.BEFORE_ALL);

        Assertions.assertTrue(hook.isDefinedAt(new StackTraceElement(HookMethods.class.getName(), "count", null, 0)));
        Assertions.assertFalse(hook.isDefinedAt(new StackTraceElement(HookMethods.class.getName(), "other", null, 0)));
    }

    private static Method method(String name) throws NoSuchMethodException {
        return HookMethods.class.getMethod(name);
    }

    public static class HookMethods {

        static int invocations;

        public static void count() {
            invocations++;
        }

        public static void fail() {
            throw new IllegalStateException("Suite action failed");
        }
    }
}
