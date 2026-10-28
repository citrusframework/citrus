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

package org.citrusframework.base;

import org.citrusframework.CitrusContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.BindToRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DefaultCitrusContextTest {

    @Test
    public void shouldParseConfiguration() {
        CitrusContext context = CitrusContext.create();
        context.parseConfiguration(FooConfig.class);

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("foo"));
        Assertions.assertEquals("Foo", context.getReferenceResolver().resolve("foo"));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("bar"));
        Assertions.assertEquals("Bar", context.getReferenceResolver().resolve("bar"));
        Assertions.assertTrue(context.getReferenceResolver().isResolvable("foobar"));
        Assertions.assertEquals("FooBar", context.getReferenceResolver().resolve("foobar"));
    }

    public static class FooConfig {
        @BindToRegistry
        String bar = "Bar";

        @BindToRegistry
        public String foo() {
            return "Foo";
        }

        @BindToRegistry(name = "foobar")
        public String bar() {
            return "FooBar";
        }
    }

    @Test
    public void shouldRaiseErrorWithNoDefaultConstructor() {
        CitrusContext context = CitrusContext.create();
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            context.parseConfiguration(NoDefaultConstructorConfig.class);
        });
        assertThat(thrown).hasMessage("Missing or non-accessible default constructor on custom configuration class");
    }

    public static class NoDefaultConstructorConfig {
        public NoDefaultConstructorConfig(String config) {
            LoggerFactory.getLogger(NoDefaultConstructorConfig.class).info(config);
        }
    }

    @Test
    public void shouldRaiseErrorWithPrivateMethod() {
        CitrusContext context = CitrusContext.create();
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            context.parseConfiguration(PrivateMethodConfig.class);
        });
        assertThat(thrown).hasMessage("Failed to invoke configuration method");
    }

    public static class PrivateMethodConfig {

        @BindToRegistry
        private String error() {
            return "should fail";
        }
    }
}
