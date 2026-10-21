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
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @since 5.2.0
 */
public class EnvironmentVariableFunctionTest extends UnitTestSupport {

    private final EnvironmentVariableFunction function = new EnvironmentVariableFunction();

    @Test
    public void testFunction() {
        String path = System.getenv("PATH");
        Assert.assertNotNull(path);
        Assert.assertEquals(function.execute(Collections.singletonList("PATH"), context), path);
    }

    @Test
    public void testFunctionDefaultValue() {
        Assert.assertEquals(function.execute(Arrays.asList("UNKNOWN_ENV_VAR", "This is a default"), context), "This is a default");
    }

    @Test
    public void testFunctionDefaultValueSupportsVariables() {
        context.setVariable("defaultValue", "This is a default");
        Assert.assertEquals(function.execute(Arrays.asList("UNKNOWN_ENV_VAR", "${defaultValue}"), context), "This is a default");
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class}, expectedExceptionsMessageRegExp = "Failed to resolve environment variable 'UNKNOWN_ENV_VAR'")
    public void testEnvironmentVariableNotFound() {
        function.execute(Collections.singletonList("UNKNOWN_ENV_VAR"), context);
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList(), context);
    }

    @Test
    public void testFunctionRegisteredInDefaultLibrary() {
        Assert.assertEquals(new DefaultFunctionLibrary().getFunction("envVar").getClass(), EnvironmentVariableFunction.class);
    }
}
