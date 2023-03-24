/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.functions;

import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.functions.JsonFunctions.jsonPath;

public class JsonFunctionsTest extends AbstractTestNGUnitTest {

    @Test
    public void testJsonPath() throws Exception {
        Assert.assertEquals(jsonPath("{\"text\": \"Some Text\"}", "$.text", context), "Some Text");
    }

    @Test
    public void testFunctionUtils() {
        context.setFunctionRegistry(new DefaultFunctionRegistry());
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:jsonPath('{\"message\": \"Hello Citrus!\"}', '$.message')", context), "Hello Citrus!");
    }
}
