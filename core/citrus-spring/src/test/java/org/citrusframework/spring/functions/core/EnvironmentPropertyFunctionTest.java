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

package org.citrusframework.spring.functions.core;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.base.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * @since 2.7.4
 */
public class EnvironmentPropertyFunctionTest extends AbstractUnitTest {

    private final Environment environment = Mockito.mock(Environment.class);
    private final EnvironmentPropertyFunction function = new EnvironmentPropertyFunction();

    @BeforeEach
    public void setup() {
        function.setEnvironment(environment);
    }

    @Test
    public void testFunction() {
        when(environment.getProperty("foo.property")).thenReturn("Citrus rocks!");
        Assertions.assertEquals("Citrus rocks!", function.execute(Collections.singletonList("foo.property"), context));
    }

    @Test
    public void testFunctionDefaultValue() {
        Assertions.assertEquals("This is a default", function.execute(Arrays.asList("bar.property", "This is a default"), context));
    }

    @Test
    public void testPropertyNotFound() {
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            function.execute(Collections.singletonList("bar.property"), context);
        });
        assertThat(thrown).hasMessageMatching("Failed to resolve property 'bar.property' in environment");
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(Collections.emptyList(), context));
    }

    @Test
    public void shouldLookupFunction() {
        Assertions.assertTrue(Function.lookup().containsKey("env"));
        Assertions.assertEquals(EnvironmentPropertyFunction.class, Function.lookup().get("env").getClass());

        Assertions.assertEquals(EnvironmentPropertyFunction.class, new DefaultFunctionLibrary().getFunction("env").getClass());
    }
}
