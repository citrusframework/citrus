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

import io.cucumber.core.backend.HookDefinition.HookType;
import io.cucumber.core.backend.Lookup;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CitrusHookDefinitionTest {

    private final Lookup lookup = new Lookup() {
        @Override
        public <T> T getInstance(Class<T> glueClass) {
            return null;
        }
    };

    @Test
    public void shouldExposeBeforeHookMetadata() throws NoSuchMethodException {
        CitrusHookDefinition hook = new CitrusHookDefinition(method("before"), "", 10000, lookup,
                "Citrus lifecycle before", HookType.BEFORE);

        assertEquals(hook.getName(), Optional.of("Citrus lifecycle before"));
        assertEquals(hook.getHookType(), Optional.of(HookType.BEFORE));
        assertEquals(hook.getOrder(), 10000);
        assertEquals(hook.getTagExpression(), "");
    }

    @Test
    public void shouldExposeAfterHookMetadata() throws NoSuchMethodException {
        CitrusHookDefinition hook = new CitrusHookDefinition(method("after"), "", 10000, lookup,
                "Citrus lifecycle after", HookType.AFTER);

        assertEquals(hook.getName(), Optional.of("Citrus lifecycle after"));
        assertEquals(hook.getHookType(), Optional.of(HookType.AFTER));
        assertEquals(hook.getOrder(), 10000);
        assertEquals(hook.getTagExpression(), "");
    }

    @Test
    public void shouldKeepEmptyMetadataForLegacyConstructor() throws NoSuchMethodException {
        CitrusHookDefinition hook = new CitrusHookDefinition(method("before"), "", 10000, lookup);

        assertEquals(hook.getName(), Optional.empty());
        assertEquals(hook.getHookType(), Optional.empty());
        assertEquals(hook.getOrder(), 10000);
        assertEquals(hook.getTagExpression(), "");
    }

    private static Method method(String name) throws NoSuchMethodException {
        return HookMethods.class.getMethod(name, Scenario.class);
    }

    public static class HookMethods {

        public void before(Scenario scenario) {
        }

        public void after(Scenario scenario) {
        }
    }
}
