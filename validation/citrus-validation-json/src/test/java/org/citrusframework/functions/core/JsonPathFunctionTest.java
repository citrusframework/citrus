/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.functions.core;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.6
 */
public class JsonPathFunctionTest extends AbstractTestNGUnitTest {

    private final JsonPathFunction function = new JsonPathFunction();

    private final String jsonSource = "{ \"person\": { \"name\": \"Sheldon\", \"age\": \"29\" } }";

    @Test
    public void testExecuteJsonPath() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.name");
        Assert.assertEquals(function.execute(parameters, context), "Sheldon");
    }

    @Test
    public void testExecuteJsonPathFunctions() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.keySet()");
        Assert.assertEquals(function.execute(parameters, context), "[name, age]");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testExecuteJsonPathUnknown() throws Exception {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.unknown");
        function.execute(parameters, context);
    }

    @Test
    public void shouldLookupFunction() {
        Assert.assertTrue(Function.lookup().containsKey("jsonPath"));
        Assert.assertEquals(Function.lookup().get("jsonPath").getClass(), JsonPathFunction.class);

        Assert.assertEquals(new DefaultFunctionLibrary().getFunction("jsonPath").getClass(), JsonPathFunction.class);
    }
}
