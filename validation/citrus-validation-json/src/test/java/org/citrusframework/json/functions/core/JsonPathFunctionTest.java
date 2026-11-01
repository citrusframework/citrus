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

package org.citrusframework.json.functions.core;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.base.functions.DefaultFunctionLibrary;
import org.citrusframework.functions.Function;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @since 2.6
 */
public class JsonPathFunctionTest extends AbstractUnitTest {

    private final JsonPathFunction function = new JsonPathFunction();

    private final String jsonSource = "{ \"person\": { \"name\": \"Sheldon\", \"age\": \"29\" } }";

    @Test
    public void testExecuteJsonPath() {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.name");
        Assertions.assertEquals("Sheldon", function.execute(parameters, context));
    }

    @Test
    public void testExecuteJsonPathFunctions() {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.keySet()");
        Assertions.assertEquals("[name, age]", function.execute(parameters, context));
    }

    @Test
    public void testExecuteJsonPathUnknown() {
        List<String> parameters = new ArrayList<>();
        parameters.add(jsonSource);
        parameters.add("$.person.unknown");
        Assertions.assertThrows(CitrusRuntimeException.class, () -> function.execute(parameters, context));
    }

    @Test
    public void shouldLookupFunction() {
        Assertions.assertTrue(Function.lookup().containsKey("jsonPath"));
        Assertions.assertEquals(JsonPathFunction.class, Function.lookup().get("jsonPath").getClass());

        Assertions.assertEquals(JsonPathFunction.class, new DefaultFunctionLibrary().getFunction("jsonPath").getClass());
    }
}
