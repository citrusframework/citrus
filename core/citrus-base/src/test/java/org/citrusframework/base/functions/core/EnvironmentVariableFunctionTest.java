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

package org.citrusframework.base.functions.core;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.base.functions.DefaultFunctionLibrary;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 5.2.0
 */
public class EnvironmentVariableFunctionTest extends UnitTestSupport {

    private final EnvironmentVariableFunction function = new EnvironmentVariableFunction();

    @Test
    public void testFunction() {
        String path = System.getenv("PATH");
        Assertions.assertNotNull(path);
        Assertions.assertEquals(path, function.execute(Collections.singletonList("PATH"), context));
    }

    @Test
    public void testFunctionDefaultValue() {
        Assertions.assertEquals("This is a default", function.execute(Arrays.asList("UNKNOWN_ENV_VAR", "This is a default"), context));
    }

    @Test
    public void testFunctionDefaultValueSupportsVariables() {
        context.setVariable("defaultValue", "This is a default");
        Assertions.assertEquals("This is a default", function.execute(Arrays.asList("UNKNOWN_ENV_VAR", "${defaultValue}"), context));
    }

    @Test
    public void testEnvironmentVariableNotFound() {
        CitrusRuntimeException thrown = assertThrows(CitrusRuntimeException.class, () -> {
            function.execute(Collections.singletonList("UNKNOWN_ENV_VAR"), context);
        });
        assertThat(thrown).hasMessage("Failed to resolve environment variable 'UNKNOWN_ENV_VAR'");
    }

    @Test
    public void testNoParameters() {
        assertThrows(InvalidFunctionUsageException.class, () -> function.execute(Collections.emptyList(), context));
    }

    @Test
    public void testFunctionRegisteredInDefaultLibrary() {
        Assertions.assertEquals(EnvironmentVariableFunction.class, new DefaultFunctionLibrary().getFunction("envVar").getClass());
    }
}
